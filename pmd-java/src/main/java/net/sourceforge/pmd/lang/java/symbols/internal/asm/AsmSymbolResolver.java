/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.asm;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.java.symbols.JModuleSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClassQueryGraph;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClassQueryGraph.ClasspathCheckResult;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.Loader.FailedLoader;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.Loader.StreamLoader;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.util.AssertionUtil;
import net.sourceforge.pmd.util.GraphUtil;

/**
 * A {@link SymbolResolver} that reads class files to produce symbols.
 */
public class AsmSymbolResolver implements SymbolResolver {
    private static final Logger LOG = LoggerFactory.getLogger(AsmSymbolResolver.class);

    static final int ASM_API_V = Opcodes.ASM9;

    private final TypeSystem ts;
    private final Classpath classLoader;

    private final ConcurrentMap<String, ClassStub> knownStubs = new ConcurrentHashMap<>();

    /**
     * Sentinel for when we fail finding a URL. This allows using a single map,
     * instead of caching failure cases separately.
     */
    private final ClassStub failed;
    private final ClasspathDependencyTracker dependencyTracker;

    public AsmSymbolResolver(TypeSystem ts, Classpath classLoader) {
        this.ts = ts;
        this.classLoader = classLoader;
        this.failed = new ClassStub(this, "/*failed-lookup*/", FailedLoader.INSTANCE, 0);
        this.dependencyTracker = new ClasspathDependencyTracker(this);
    }

    Set<String> getQueriedInternalNames() {
        return knownStubs.keySet();
    }

    long getStubHash(String internalName) {
        ClassStub stub = knownStubs.get(internalName);
        if (stub == null || stub == failed) {
            return 0;
        }
        return stub.getAbiFingerprint();
    }

    @Override
    public @Nullable ClassStub resolveClassFromBinaryName(@NonNull String binaryName, ClasspathRequest origin) {
        AssertionUtil.requireParamNotNull("binaryName", binaryName);

        String internalName = getInternalName(binaryName);

        ClassStub found = knownStubs.computeIfAbsent(internalName, iname -> {
            @Nullable InputStream inputStream = getStreamOfInternalName(iname);
            if (inputStream == null) {
                return failed;
            }

            return new ClassStub(this, iname, new StreamLoader(binaryName, inputStream), ClassStub.UNKNOWN_ARITY);
        });

        if (!found.hasCanonicalName()) {
            // note: this check needs to be done outside of computeIfAbsent
            //  to prevent recursive updates of the knownStubs map.
            knownStubs.put(internalName, failed);
            found = failed;
        }

        if (found == failed) { // NOPMD CompareObjectsWithEquals
            found = null;
        }
        dependencyTracker.recordClasspathRequest(origin, internalName);
        return found;
    }

    @Override
    public @Nullable JModuleSymbol resolveModule(@NonNull String moduleName, ClasspathRequest origin) {
        // by convention try to load module-info via "moduleName/module-info.class". The used
        // classloader will need to handle this case to return the correct module-info.class for the
        // requested module. See impl of ClasspathClassLoader in pmd-core.
        InputStream inputStream = classLoader.findResource(moduleName + "/module-info.class");
        if (inputStream != null) {
            return new ModuleStub(this, moduleName, new StreamLoader(moduleName, inputStream));
        }
        return null;
    }

    TypeSystem getTypeSystem() {
        return ts;
    }

    static @NonNull String getInternalName(@NonNull String binaryName) {
        return binaryName.replace('.', '/');
    }

    @Nullable
    InputStream getStreamOfInternalName(String internalName) {
        return classLoader.findResource(internalName + ".class");
    }

    /*
       These methods return an unresolved symbol if the url is not found.
     */

    @Nullable ClassStub resolveFromInternalNameCannotFail(@Nullable String internalName, ClasspathRequest request) {
        if (internalName == null) {
            return null;
        }
        return resolveFromInternalNameCannotFail(internalName, request, ClassStub.UNKNOWN_ARITY);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals") // ClassStub
    @NonNull ClassStub resolveFromInternalNameCannotFail(@NonNull String internalName, ClasspathRequest request, int observedArity) {
        return knownStubs.compute(internalName, (iname, prev) -> {
            if (prev != null) {
                dependencyTracker.recordClasspathRequest(request, prev.getInternalName());
                return prev;
            }
            @Nullable InputStream inputStream = getStreamOfInternalName(iname);
            Loader loader = inputStream == null ? FailedLoader.INSTANCE : new StreamLoader(internalName, inputStream);
            ClassStub result = new ClassStub(this, iname, loader, observedArity);
            dependencyTracker.recordClasspathRequest(request, result.getInternalName());
            return result;
        });
    }

    @Override
    public void logStats() {
        int numParsed = 0;
        int numFailed = 0;
        int numFailedQueries = 0;
        int numNotParsed = 0;

        for (ClassStub stub : knownStubs.values()) {
            if (stub == failed) { // NOPMD CompareObjectsWithEquals
                // Note that failed queries may occur under normal circumstances.
                // Eg package names may be queried just to figure
                // out whether they're packages or classes.
                numFailedQueries++;
            } else if (stub.isNotParsed()) {
                numNotParsed++;
            } else if (!stub.isFailed()) {
                numParsed++;
            } else {
                numFailed++;
            }
        }

        LOG.trace("Of {} distinct queries to the classloader, {} queries failed, "
                        + "{} classes were found and parsed successfully, "
                        + "{} were found but failed parsing (!), "
                        + "{} were found but never parsed.",
                knownStubs.size(), numFailedQueries, numParsed, numFailed, numNotParsed);

    }

    public void writeReducedGraph(Path toPath) throws IOException {
        // todo do that asynchronously during reporting (it takes a second to reduce the graph. Probably better
        //  algorithms could be used)
        ClassDependencyGraph summaryGraph = dependencyTracker.makeSummaryGraph();
        try (OutputStream out = Files.newOutputStream(toPath)) {
            summaryGraph.serialize(out);
        }
        LOG.debug("Wrote binary dependency graph to {}", toPath);

        if (LOG.isTraceEnabled()) {
            toPath = toPath.getParent().resolve("depgraph-full.dot");
            try (BufferedWriter writer = Files.newBufferedWriter(toPath)) {
                GraphUtil.toDot(writer, dependencyTracker.asDotGraph());
            }
            LOG.debug("Wrote class dependency graph to {}", toPath);

            toPath = toPath.getParent().resolve("depgraph-reduced.gexf");
            try (BufferedWriter writer = Files.newBufferedWriter(toPath)) {
                GraphUtil.toGexf(writer, summaryGraph.asWriteableGraph());
            }
            LOG.debug("Wrote reduced dependency graph to {}", toPath);
        }
    }

    void recordOuterClass(ClassStub classStub, @Nullable ClassStub outerClass) {
        if (outerClass != null) {
            dependencyTracker.recordClasspathRequest(classStub.getClasspathRequest(), outerClass.getInternalName());
        }
    }

    public ClasspathCheckResult readClasspathDependencyCache(Path classGraphCache) throws IOException {
        if (!Files.exists(classGraphCache)) {
            return ClasspathCheckResult.noCacheFile();
        }
        ClassQueryGraph cachedDepGraph;
        try(InputStream in = Files.newInputStream(classGraphCache)) {
            cachedDepGraph = ClassDependencyGraph.deserialize(in);
        }
        // compute out-of-date files
        ClasspathCheckResult result = cachedDepGraph.checkClasspathIsUpToDate(this);
        // restore cached dependencies into the tracker
        dependencyTracker.restoreCachedGraph(result);
        return result;
    }
}
