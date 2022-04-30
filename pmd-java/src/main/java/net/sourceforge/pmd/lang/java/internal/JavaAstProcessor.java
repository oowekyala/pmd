/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import static net.sourceforge.pmd.lang.java.symbols.table.internal.JavaSemanticErrors.CANNOT_RESOLVE_SYMBOL;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.event.Level;

import net.sourceforge.pmd.benchmark.TimeTracker;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.internal.UnresolvedClassStore;
import net.sourceforge.pmd.lang.java.symbols.internal.ast.SymbolResolutionPass;
import net.sourceforge.pmd.lang.java.symbols.table.internal.ReferenceCtx;
import net.sourceforge.pmd.lang.java.symbols.table.internal.SymbolTableResolver;
import net.sourceforge.pmd.lang.java.types.TypeInternals;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger.SimpleLogger;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger.VerboseLogger;

/**
 * Processes the output of the parser before rules get access to the AST.
 * This performs all semantic analyses in layered passes.
 *
 * <p>This is the root context object for file-specific context. Instances
 * do not need to be thread-safe. Global information about eg the classpath
 * is held in a {@link TypeSystem} instance.
 *
 * <p>The object lives as long as a file, it is accessible from nodes
 * using {@link InternalApiBridge#getProcessor(JavaNode)}.
 */
public final class JavaAstProcessor {

    /**
     * FIXME get rid of that, this prevents both ClassLoader and TypeSystem
     *  to be garbage-collected, which is an important memory leak. Will be
     *  fixed by https://github.com/pmd/pmd/issues/3782 (Language Lifecycle)
     */
    private static final Map<ClassLoader, TypeSystem> TYPE_SYSTEMS = new IdentityHashMap<>();
    private static final Level INFERENCE_LOG_LEVEL;


    static {
        Level level;
        try {
            level = Level.valueOf(System.getenv("PMD_DEBUG_LEVEL").toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            level = null;
        }
        INFERENCE_LOG_LEVEL = level;
    }


    private final TypeInferenceLogger typeInferenceLogger;
    private final SemanticErrorReporter logger;
    private final LanguageVersion languageVersion;
    private final TypeSystem typeSystem;

    private final UnresolvedClassStore unresolvedTypes;


    private JavaAstProcessor(TypeSystem typeSystem,
                             SemanticErrorReporter logger,
                             TypeInferenceLogger typeInfLogger,
                             LanguageVersion languageVersion) {

        this.logger = logger;
        this.typeInferenceLogger = typeInfLogger;
        this.languageVersion = languageVersion;

        this.typeSystem = typeSystem;
        unresolvedTypes = new UnresolvedClassStore(typeSystem);
    }

    public UnresolvedClassStore getUnresolvedStore() {
        return unresolvedTypes;
    }

    static TypeInferenceLogger defaultTypeInfLogger() {
        if (INFERENCE_LOG_LEVEL == Level.TRACE) {
            return new VerboseLogger(System.err);
        } else if (INFERENCE_LOG_LEVEL == Level.DEBUG) {
            return new SimpleLogger(System.err);
        } else {
            return TypeInferenceLogger.noop();
        }
    }


    /**
     * Find a symbol from the auxclasspath. If not found, will create
     * an unresolved symbol.
     */
    public @NonNull JClassSymbol findSymbolCannotFail(String name) {
        return findSymbolCannotFail(null, name);
    }

    /**
     * Find a symbol from the auxclasspath. If not found, will create
     * an unresolved symbol, and may report the failure if the location is non-null.
     */
    public @NonNull JClassSymbol findSymbolCannotFail(@Nullable JavaNode location, String canoName) {
        JClassSymbol found = getSymResolver().resolveClassFromCanonicalName(canoName);
        if (found == null) {
            if (location != null) {
                reportCannotResolveSymbol(location, canoName);
            }
            return makeUnresolvedReference(canoName, 0);
        }
        return found;
    }

    public void reportCannotResolveSymbol(@NonNull JavaNode location, String canoName) {
        getLogger().warning(location, CANNOT_RESOLVE_SYMBOL, canoName);
    }

    public JClassSymbol makeUnresolvedReference(String canonicalName, int typeArity) {
        return unresolvedTypes.makeUnresolvedReference(canonicalName, typeArity);
    }

    public JClassSymbol makeUnresolvedReference(JTypeDeclSymbol outer, String simpleName, int typeArity) {
        if (outer instanceof JClassSymbol) {
            return unresolvedTypes.makeUnresolvedReference((JClassSymbol) outer, simpleName, typeArity);
        }
        return makeUnresolvedReference("error." + simpleName, typeArity);
    }

    public SymbolResolver getSymResolver() {
        return typeSystem.symbolResolver();
    }

    public SemanticErrorReporter getLogger() {
        return logger;
    }

    public LanguageVersion getLanguageVersion() {
        return languageVersion;
    }

    public int getJdkVersion() {
        return ((JavaLanguageHandler) languageVersion.getLanguageVersionHandler()).getJdkVersion();
    }

    /**
     * Performs semantic analysis on the given source file.
     */
    public void process(ASTCompilationUnit acu) {

        SymbolResolver knownSyms = TimeTracker.bench("1. Symbol resolution", () -> SymbolResolutionPass.traverse(this, acu));

        // Now symbols are on the relevant nodes
        // Improve the resolver so that it always picks the types
        // declared in the compilation unit from our AST symbols.
        // Note: the type system is local to this JavaAstProcessor.
        TypeInternals.transformResolver(this.typeSystem, r -> SymbolResolver.layer(knownSyms, r));

        // this needs to be initialized before the symbol table resolution
        // as scopes depend on type resolution in some cases.
        InternalApiBridge.initTypeResolver(acu, this, typeInferenceLogger);

        TimeTracker.bench("2. Symbol table resolution", () -> SymbolTableResolver.traverse(this, acu));
        TimeTracker.bench("3. AST disambiguation", () -> InternalApiBridge.disambigWithCtx(NodeStream.of(acu), ReferenceCtx.root(this, acu)));
        TimeTracker.bench("4. Force type resolution", () -> InternalApiBridge.forceTypeResolutionPhase(this, acu));
        TimeTracker.bench("5. Comment assignment", () -> InternalApiBridge.assignComments(acu));
        TimeTracker.bench("6. Usage resolution", () -> InternalApiBridge.usageResolution(this, acu));
        TimeTracker.bench("7. Override resolution", () -> InternalApiBridge.overrideResolution(this, acu));
    }

    public TypeSystem getTypeSystem() {
        return typeSystem;
    }

    public static JavaAstProcessor create(TypeSystem typeSystem,
                                          LanguageVersion languageVersion,
                                          SemanticErrorReporter logger) {

        return new JavaAstProcessor(
            typeSystem,
            logger,
            defaultTypeInfLogger(),
            languageVersion
        );
    }

    private static JavaAstProcessor create(ClassLoader classLoader,
                                           LanguageVersion languageVersion,
                                           SemanticErrorReporter logger,
                                           TypeInferenceLogger typeInfLogger) {
        // fixme remove this static cache
        TypeSystem globalTypeSystem = TYPE_SYSTEMS.computeIfAbsent(classLoader, TypeSystem::usingClassLoaderClasspath);

        return new JavaAstProcessor(
            globalTypeSystem.newScope(), // create a new local type system
            logger,
            typeInfLogger,
            languageVersion
        );
    }


    public static JavaAstProcessor create(ClassLoader classLoader,
                                          LanguageVersion languageVersion,
                                          SemanticErrorReporter logger) {
        return create(classLoader, languageVersion, logger, defaultTypeInfLogger());
    }

    public static JavaAstProcessor create(TypeSystem typeSystem,
                                          LanguageVersion languageVersion,
                                          SemanticErrorReporter semanticLogger,
                                          TypeInferenceLogger typeInfLogger) {
        return new JavaAstProcessor(
            typeSystem,
            semanticLogger,
            typeInfLogger,
            languageVersion
        );
    }

}
