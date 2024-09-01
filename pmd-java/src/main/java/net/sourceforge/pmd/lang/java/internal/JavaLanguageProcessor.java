/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.InternalApiBridge;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.ast.Parser.ParserTask;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.ast.SemanticException;
import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.impl.BatchLanguageProcessor;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge.SymbolCollector;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge.SymbolParser;
import net.sourceforge.pmd.lang.java.ast.JavaParser;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageProperties.InferenceLoggingVerbosity;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.BaseContextNodeTestFun;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.GetCommentOnFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.GetModifiersFun;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.MatchesSignatureFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.MetricFunction;
import net.sourceforge.pmd.lang.java.rule.xpath.internal.NodeIsFunction;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
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
    private TypeSystem typeSystem;

    public JavaLanguageProcessor(JavaLanguageProperties properties, TypeSystem typeSystem) {
        super(properties);
        this.typeSystem = typeSystem;

        String suppressMarker = properties.getSuppressMarker();
        this.parser = new JavaParser(suppressMarker, this, true);
        this.parserWithoutProcessing = new JavaParser(suppressMarker, this, false);
    }

    public JavaLanguageProcessor(JavaLanguageProperties properties) {
        this(properties, TypeSystem.usingClassLoaderClasspath(properties.getAnalysisClassLoader()));
        LOG.debug("Using analysis classloader: {}", properties.getAnalysisClassLoader());
    }


    static <T> void processInParallelStream(Stream<T> data, int parallelism, Consumer<T> task) {
        if (parallelism == 0) {
            data.sequential().forEach(task);
            return;
        }
        ForkJoinPool forkJoinPool = null;
        try {
            forkJoinPool = new ForkJoinPool(parallelism);
            forkJoinPool.submit(() -> data.parallel().forEach(task)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
            }
        }

    }

    class SourceSymbolResolver implements SymbolCollector {
        Map<String, JClassSymbol> binaryNamesToSymbols = new ConcurrentHashMap<>();
        final AnalysisTask task;

        SourceSymbolResolver(AnalysisTask task) {
            this.task = task;
        }


        void readOneFile(TextFile javaFile) {
            try (TextDocument doc = TextDocument.create(javaFile)) {
                SemanticErrorReporter reporter = SemanticErrorReporter.reportToLogger(task.getMessageReporter());
                @SuppressWarnings("PMD.CloseResource")
                ParserTask parserTask = new ParserTask(doc,
                                                       reporter,
                                                       task.getLpRegistry());

                SymbolParser parser = net.sourceforge.pmd.lang.java.ast.InternalApiBridge.getSymbolParser(JavaLanguageProcessor.this);


                parser.parseFile(parserTask, this);

                SemanticException semanticError = reporter.getFirstError();
                if (semanticError != null) {
                    // cause a processing error to be reported and rule analysis to be skipped
                    throw semanticError;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void collectSources() {

            processInParallelStream(
                task.getFiles().stream(), task.getThreadCount(),
                this::readOneFile);

        }

        @Override
        public void accept(JClassSymbol incompleteSymbol) {
            System.out.println("Found " + incompleteSymbol);
            binaryNamesToSymbols.put(incompleteSymbol.getBinaryName(), incompleteSymbol);
        }
    }

    @Override
    public @NonNull AutoCloseable launchAnalysis(@NonNull AnalysisTask task) {
        // The given analysis task has all files to analyse, not only the ones for this language.
        List<TextFile> files = new ArrayList<>(task.getFiles());
        files.removeIf(it -> !it.getLanguageVersion().getLanguage().equals(getLanguage()));
        AnalysisTask newTask = InternalApiBridge.taskWithFiles(task, files);

        task.getRulesets().initializeRules(task.getLpRegistry(), task.getMessageReporter());

        SourceSymbolResolver collector = new SourceSymbolResolver(newTask);
        collector.collectSources();
        return () -> {};
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
        return AnnotationSuppressionUtil.ALL_JAVA_SUPPRESSORS;
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
        super.close();
    }
}
