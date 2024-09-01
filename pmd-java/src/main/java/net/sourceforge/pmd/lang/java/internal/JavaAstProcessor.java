/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import static net.sourceforge.pmd.lang.java.symbols.table.internal.JavaSemanticErrors.CANNOT_RESOLVE_SYMBOL;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.benchmark.TimeTracker;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JModuleSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.internal.UnresolvedClassStore;
import net.sourceforge.pmd.lang.java.symbols.internal.ast.SymbolResolutionPass;
import net.sourceforge.pmd.lang.java.symbols.table.internal.ReferenceCtx;
import net.sourceforge.pmd.lang.java.symbols.table.internal.SymbolTableResolver;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger;

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
    private final TypeInferenceLogger typeInferenceLogger;
    private final JavaLanguageProcessor globalProc;
    private final SemanticErrorReporter logger;

    private SymbolResolver symResolver;

    private final UnresolvedClassStore unresolvedTypes;
    private final ASTCompilationUnit acu;


    private JavaAstProcessor(JavaLanguageProcessor globalProc,
                             SemanticErrorReporter logger,
                             TypeInferenceLogger typeInfLogger,
                             ASTCompilationUnit acu) {

        this.symResolver = globalProc.getTypeSystem().bootstrapResolver();
        this.globalProc = globalProc;
        this.logger = logger;
        this.typeInferenceLogger = typeInfLogger;
        this.unresolvedTypes = new UnresolvedClassStore(globalProc.getTypeSystem());
        this.acu = acu;
    }

    public UnresolvedClassStore getUnresolvedStore() {
        return unresolvedTypes;
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
        return symResolver;
    }

    public SemanticErrorReporter getLogger() {
        return logger;
    }

    public int getJdkVersion() {
        return JavaLanguageProperties.getInternalJdkVersion(acu.getLanguageVersion());
    }

    /**
     * Performs semantic analysis on the given source file.
     */
    public void process() {
        process(false);
    }

    private void process(boolean skeletton) {
        SymbolResolver symResolver = this.symResolver;
        // poison the resolver, to make sure that we are not querying the auxclasspath at this point
        this.symResolver = PoisonedResolver.INSTANCE;

        SymbolResolver knownSyms = TimeTracker.bench("Symbol resolution", () -> SymbolResolutionPass.traverse(this, acu));

        // Now symbols are on the relevant nodes
        symResolver = SymbolResolver.layer(knownSyms, symResolver);


        // this needs to be initialized before the symbol table resolution
        // as scopes depend on type resolution in some cases.
        InternalApiBridge.initTypeResolver(acu, this, typeInferenceLogger);

        TimeTracker.bench("Symbol table resolution", () -> SymbolTableResolver.traverse(this, acu));

        // We are not poisoned anymore. TODO do disambig
        this.symResolver = symResolver;
        if (skeletton) {
            return;
        }

        TimeTracker.bench("AST disambiguation", () -> InternalApiBridge.disambigWithCtx(acu, ReferenceCtx.root(this, acu)));
        if (globalProc.getProperties().getProperty(JavaLanguageProperties.INTERNAL_DO_STRICT_TYPERES)) {
            TimeTracker.bench("Force type resolution", () -> InternalApiBridge.forceTypeResolutionPhase(this, acu));
        }
        TimeTracker.bench("Comment assignment", () -> InternalApiBridge.assignComments(acu));
        TimeTracker.bench("Usage resolution", () -> InternalApiBridge.usageResolution(this, acu));
        TimeTracker.bench("Override resolution", () -> InternalApiBridge.overrideResolution(this, acu));
    }

    public void processSkeletton() {
        process(true);
    }

    static class PoisonedResolver implements SymbolResolver {
        static final PoisonedResolver INSTANCE = new PoisonedResolver();

        @Override
        public @Nullable JClassSymbol resolveClassFromBinaryName(@NonNull String binaryName) {
            throw new IllegalStateException("Should not call the symbol resolver at this stage, asked for " + binaryName);
        }

        @Override
        public @Nullable JModuleSymbol resolveModule(@NonNull String moduleName) {
            throw new IllegalStateException("Should not call the symbol resolver at this stage, asked for " + moduleName);
        }

        @Override
        public void logStats() {

        }
    }

    public TypeSystem getTypeSystem() {
        return globalProc.getTypeSystem();
    }


    public static void processSkeletton(JavaLanguageProcessor globalProcessor, SemanticErrorReporter err, ASTCompilationUnit ast) {

        JavaAstProcessor astProc = new JavaAstProcessor(
            globalProcessor,
            err,
            globalProcessor.newTypeInfLogger(),
            ast
        );

        astProc.processSkeletton();
    }

    public static void process(JavaLanguageProcessor globalProcessor,
                                          SemanticErrorReporter semanticErrorReporter,
                                           ASTCompilationUnit ast) {
        process(globalProcessor, semanticErrorReporter, globalProcessor.newTypeInfLogger(), ast);
    }

    public static void process(JavaLanguageProcessor globalProcessor,
                                          SemanticErrorReporter semanticErrorReporter,
                                          TypeInferenceLogger typeInfLogger,
                                           ASTCompilationUnit ast) {


        JavaAstProcessor astProc = new JavaAstProcessor(
            globalProcessor,
            semanticErrorReporter,
            typeInfLogger,
            ast
        );

        astProc.process();
    }
}
