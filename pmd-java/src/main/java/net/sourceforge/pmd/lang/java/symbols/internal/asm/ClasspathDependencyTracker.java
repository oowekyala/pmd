package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.Vertex;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClassQueryGraph.ClasspathCheckResult;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.DependencyNode;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathRequest.ClassFileRequest;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathRequest.DependencyType;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathRequest.NoOrigin;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathRequest.SourceFileRequest;
import net.sourceforge.pmd.util.GraphUtil.DotColor;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;

/**
 * A fine-grained dependency graph that stores the dependencies between analyzed files.
 *
 * <p>The analyzed files each have a set of dependencies on other files or on some jars
 * on the classpath.
 *
 * <p>The cached result of a file analysis is fresh if all the requests to the symbol resolver
 * made during the analysis (parsing, type resolution, and rule analysis) of the file will return
 * the same thing as they did during the initial processing. For instance, resolution of an ambiguous
 * name in a file in package p depends on the classes in that package, and also on the imported files.
 * The disambiguation process makes many requests to the classloader (symbol resolver).
 * All requests made to the symbol resolver count as dependencies of this file.
 *
 * <p>During file analysis, each request to the symbol resolver must be done with a specific request ID
 * which is associated to the file-id. This set of strings is the set of "request dependencies" of the file.
 * After analysis, the map of requests to results is cached. The results are either "failed" or a .class
 * file, with its checksum (this could be an ABI checksum for fine granularity).
 *
 * <p>Before the next analysis, for every file that did not change, the requests dependencies of that file are
 * replayed against the current classloader. If any request returns a different result, the dependencies are
 * invalidated and the file is reanalyzed.
 *
 * <p>The current incremental analysis will invalidate all files if any classpath entry has been updated. If
 * one file has been updated and recompiled, all files will therefore be reanalyzed. The new dependency mechanism
 * allows more fine-grained reanalysis, without ignoring dependencies.
 *
 * <p>Where this mechanism will be very useful is to process files that have not yet been compiled. To do this,
 * a first pass will parse all files in the analysis (possibly ignoring code blocks, because we only need symbol
 * signatures anyway). This allows us to build a symbol resolver that considers both classpath, and source path.
 * After the first pass, symbols are still incomplete as their signatures require name resolution. A second pass
 * resolves signatures and promotes these to full-fledged symbols. Note however that they don't have direct access
 * to the AST (we throw away the AST and backing text file to store symbols in a more compact way). Note that name
 * resolution here contributes "request dependencies" too. Once this is done, we have a symbol resolver that represents
 * the full project and does not require class files for the analysed files anymore.
 *
 * <p>At this point these symbols need to be cached for the next analysis (along with their hashes and dependency set).
 * Then rule execution can proceed. This requires full reparsing of changed files. Parsing the file entirely may require
 * more dependencies than just the ones required to resolve signatures. The additional dependencies are cached separately.
 *
 * <p>On the next analysis, if any file was changed, it is first shallow-parsed with the symbol-only parser. Then symbol
 * resolution is redone, and its symbols rehashed. If the hash is the same, then files that depend on that file are not invalidated.
 *
 * <p>At this point we can look forward to full multi-file analysis, where the AST of a file is accessible from the AST
 * of other files. This creates a stronger kind of dependency. There are two kinds of dependencies: either signature-only,
 * or full-text.
 *
 *
 */
public class ClasspathDependencyTracker {

    private final Map<FileId, SourceRequests> sourceDeps;
    private final Map<String, ClassRequests> binaryDeps;
    private final AsmSymbolResolver resolver;

    ClasspathDependencyTracker(AsmSymbolResolver resolver) {
        this.resolver = resolver;
        sourceDeps = new ConcurrentHashMap<>();
        binaryDeps = new ConcurrentHashMap<>();
    }


    /**
     * Record that the given origin made a classpath request for the given binary name.
     *
     * @param internalName Binary name for the request
     * @param request The origin file
     */
    void recordClasspathRequest(ClasspathRequest request, @NonNull String internalName) {
        if (request == NoOrigin.INSTANCE) {
            return;
        }
        if (request instanceof SourceFileRequest) {
            SourceFileRequest sourceRequest = (SourceFileRequest) request;

            sourceDeps.compute(sourceRequest.origin, (fid, entries) -> {
                if (entries == null) {
                    entries = new SourceRequests();
                }
                entries.record(internalName, sourceRequest.type == DependencyType.SIGNATURE);
                return entries;
            });
        } else if (request instanceof ClassFileRequest) {
            ClassFileRequest classRequest = (ClassFileRequest) request;
            binaryDeps.compute(classRequest.internalName, (k, entries) -> {
                if (entries == null) {
                    entries = new ClassRequests();
                }
                entries.record(internalName);
                return entries;
            });
        }
    }

    /**
     * Make the final dependency graph by merging the info we have about class
     * and source file dependencies into a single graph. This structure can then
     * be persisted between runs.
     */
    public ClassDependencyGraph makeSummaryGraph() {
        ClassDependencyGraph graph = new ClassDependencyGraph();
        for (String internalName : resolver.getQueriedInternalNames()) {

            Vertex<DependencyNode> fromClass = graph.addClassLeaf(internalName, resolver.getStubHash(internalName));
            ClassRequests req = binaryDeps.get(internalName);
            if (req == null) {
                continue;
            }
            for (String dep : req.dependenciesInternalNames) {
                Vertex<DependencyNode> toClass = graph.addClassLeaf(dep, resolver.getStubHash(dep));
                graph.recordDependency(fromClass, toClass);
            }
        }

        sourceDeps.forEach(
            (fileId, requests) -> {
                Vertex<DependencyNode> fromSource = graph.addSourceLeaf(fileId);
                for (String req : requests.byInternalName.keySet()) {
                    Vertex<DependencyNode> toClass = graph.addClassLeaf(req, resolver.getStubHash(req));
                    graph.recordDependency(fromSource, toClass);
                }
            }
        );

        graph.finalizeGraph();

        return graph;
    }


    public void restoreCachedGraph(ClasspathCheckResult result) {

    }


    /**
     * Note that this graph only contains classes from the classpath
     * and their dependencies. It does not contain source files.
     */
    public DotGraphDescription<?> asDotGraph() {
        return new DotGraphDescription<>(
            resolver.getQueriedInternalNames(),
            v -> {
                ClassRequests cr = binaryDeps.get(v);
                if (cr == null) {
                    return Collections.emptySet();
                } else {
                    return cr.dependenciesInternalNames;
                }
            },
            v -> DotColor.BLACK,
            v -> v.replace('/', '.')
        );
    }


    private static final class SourceRequests {
        private final HashMap<String, Boolean> byInternalName = new HashMap<>();

        void record(String internalName, boolean isSigOnly) {
            byInternalName.compute(internalName, (name2, data) -> data == null ? isSigOnly : data && isSigOnly);
        }
    }

    private static final class ClassRequests {
        private final HashSet<String> dependenciesInternalNames = new HashSet<>();

        void record(String binaryName) {
            dependenciesInternalNames.add(binaryName);
        }
    }

}
