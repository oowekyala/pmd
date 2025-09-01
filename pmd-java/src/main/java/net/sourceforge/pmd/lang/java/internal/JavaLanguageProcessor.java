/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.InternalApiBridge;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.impl.AbstractPMDProcessor;
import net.sourceforge.pmd.lang.impl.BatchLanguageProcessor;
import net.sourceforge.pmd.lang.java.ast.JavaParser;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageProperties.InferenceLoggingVerbosity;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.BaseContextNodeTestFun;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.GetCommentOnFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.GetModifiersFun;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.MatchesSignatureFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.MetricFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.NodeIsFunction;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.AsmSymbolResolver;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClasspathCheckResult;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger.SimpleLogger;
import net.sourceforge.pmd.lang.java.types.internal.infer.TypeInferenceLogger.VerboseLogger;
import net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathHandler;
import net.sourceforge.pmd.reporting.ViolationDecorator;
import net.sourceforge.pmd.reporting.ViolationSuppressor;
import net.sourceforge.pmd.util.designerbindings.DesignerBindings;

/**
 * @author Clément Fournier
 */
public class JavaLanguageProcessor extends BatchLanguageProcessor<JavaLanguageProperties>
    implements LanguageVersionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JavaLanguageProcessor.class);

    private final LanguageMetricsProvider myMetricsProvider = new JavaMetricsProvider();
    private final JavaParser parser;
    private final JavaParser parserWithoutProcessing;
    private final boolean firstClassLombok;
    private TypeSystem typeSystem;
    private JavaCacheManager javaCacheManager;

    public JavaLanguageProcessor(JavaLanguageProperties properties, TypeSystem typeSystem) {
        super(properties);
        this.typeSystem = typeSystem;

        String suppressMarker = properties.getSuppressMarker();
        this.parser = new JavaParser(suppressMarker, this, true);
        this.parserWithoutProcessing = new JavaParser(suppressMarker, this, false);
        this.firstClassLombok = properties.getProperty(JavaLanguageProperties.FIRST_CLASS_LOMBOK);
    }

    public JavaLanguageProcessor(JavaLanguageProperties properties) {
        this(properties, TypeSystem.usingClassLoaderClasspath(properties.getAnalysisClassLoader()));
        LOG.debug("Using analysis classloader: {}", properties.getAnalysisClassLoader());
    }

    @Override
    public @NonNull AutoCloseable launchAnalysis(@NonNull AnalysisTask task) {
        // The given analysis task has all files to analyse, not only the ones for this language.
        List<TextFile> files = new ArrayList<>(task.getFiles());
        files.removeIf(it -> !it.getLanguageVersion().getLanguage().equals(getLanguage()));
        AnalysisTask newTask = InternalApiBridge.taskWithFiles(task, files);

        task.getRulesets().initializeRules(task.getLpRegistry(), task.getMessageReporter());

        Path javaCache = task.getCacheDir().getLanguageCache(getLanguage());
        javaCacheManager = new JavaCacheManager(javaCache);
        @Nullable ClasspathCheckResult classpathCheck = readClassGraphCache();
        // TODO augment the AnalysisCache with the result.

        // launch processing.
        AbstractPMDProcessor processor = AbstractPMDProcessor.newFileProcessor(newTask);
        // If this is a multi-threaded processor, this call is non-blocking,
        // the call to close on the returned instance blocks instead.
        processor.processFiles();
        return processor;
    }

    private ClasspathCheckResult readClassGraphCache() {
        try {
            if (Files.exists(javaCacheManager.getReducedDepGraphPath())) {

                AsmSymbolResolver symbolResolver = (AsmSymbolResolver) typeSystem.bootstrapResolver();
                ClasspathCheckResult result = symbolResolver.readClasspathDependencyCache(javaCacheManager);

                if (result.allFilesNeedToBeProcessedAgain()) {
                    LOG.debug("All files will need to be processed again");
                } else {
                    LOG.debug("Dependency analysis found {} changed files", result.getChangedFiles().size());
                }
                return result;
            } else {
                LOG.debug("No dependency information from previous run");
            }
        } catch (IOException ioe) {
            LOG.debug("Problem while reading cache file", ioe);
        }
        return null;
    }

    @Override
    public @NonNull LanguageVersionHandler services() {
        return this;
    }

    @Override
    public Parser getParser() {
        return parser;
    }

    public JavaParser getParserWithoutProcessing() {
        return parserWithoutProcessing;
    }

    public TypeSystem getTypeSystem() {
        return typeSystem;
    }

    public boolean hasFirstClassLombokSupport() {
        return firstClassLombok;
    }

    TypeInferenceLogger newTypeInfLogger() {
        InferenceLoggingVerbosity verbosity = getProperties().getProperty(JavaLanguageProperties.INTERNAL_INFERENCE_LOGGING_VERBOSITY);
        if (verbosity == InferenceLoggingVerbosity.VERBOSE) {
            return new VerboseLogger(System.err);
        } else if (verbosity == InferenceLoggingVerbosity.SIMPLE) {
            return new SimpleLogger(System.err);
        } else {
            return TypeInferenceLogger.noop();
        }
    }

    @Override
    public DesignerBindings getDesignerBindings() {
        return JavaDesignerBindings.INSTANCE;
    }

    @Override
    public XPathHandler getXPathHandler() {
        return XPATH_HANDLER;
    }

    @Override
    public List<ViolationSuppressor> getExtraViolationSuppressors() {
        return JavaAnnotationSuppressor.ALL_JAVA_SUPPRESSORS;
    }

    @Override
    public ViolationDecorator getViolationDecorator() {
        return JavaViolationDecorator.INSTANCE;
    }

    @Override
    public LanguageMetricsProvider getLanguageMetricsProvider() {
        return myMetricsProvider;
    }

    private static final XPathHandler XPATH_HANDLER =
        XPathHandler.getHandlerForFunctionDefs(
            BaseContextNodeTestFun.TYPE_IS_EXACTLY,
            BaseContextNodeTestFun.TYPE_IS,
            BaseContextNodeTestFun.HAS_ANNOTATION,
            MatchesSignatureFunction.INSTANCE,
            NodeIsFunction.INSTANCE,
            GetModifiersFun.GET_EFFECTIVE,
            GetModifiersFun.GET_EXPLICIT,
            MetricFunction.INSTANCE,
            GetCommentOnFunction.INSTANCE
        );

    public void setTypeSystem(TypeSystem ts) {
        this.typeSystem = Objects.requireNonNull(ts);
    }

    @Override
    public void close() throws Exception {
        this.typeSystem.logStats();
        if (javaCacheManager != null) {
            AsmSymbolResolver resolver = (AsmSymbolResolver) this.typeSystem.bootstrapResolver();
            resolver.writeReducedGraph(javaCacheManager);
        }
        super.close();
    }

    public static final class JavaCacheManager {
        private final Path root;

        private JavaCacheManager(Path root) {
            this.root = root;
        }

        /**
         * Path to the reduced dependency graph. Reducing the graph
         * may take a second so it is better to do it at the end of
         * processing, concurrently with reporting, than to do it
         * at the start of the analysis and block the main thread.
         * Reducing the graph allows for faster up-to-date checks,
         * but throws away some dependency information. This is why
         * we also persist the full, unreduced graph. That one is used
         * to initialize the dependency tracker's internal structure.
         */
        public Path getReducedDepGraphPath() {
            return root.resolve("dep-graph-reduced.bin.gz");
        }

        /**
         * The full dependency graph.
         *
         * @see #getReducedDepGraphPath()
         */
        public Path getSourceDepGraphPath() {
            return root.resolve("dep-graph-full.bin.gz");
        }
    }
}
