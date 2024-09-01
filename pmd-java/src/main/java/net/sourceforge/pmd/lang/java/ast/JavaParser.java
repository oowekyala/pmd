/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.AstInfo;
import net.sourceforge.pmd.lang.ast.FileAnalysisException;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccTokenDocument.TokenDocumentBehavior;
import net.sourceforge.pmd.lang.ast.impl.javacc.JjtreeParserAdapter;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge.SymbolParser;
import net.sourceforge.pmd.lang.java.ast.internal.LanguageLevelChecker;
import net.sourceforge.pmd.lang.java.ast.internal.ReportingStrategy;
import net.sourceforge.pmd.lang.java.internal.JavaAstProcessor;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageProcessor;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageProperties;

/**
 * Adapter for the JavaParser, using the specified grammar version.
 *
 * @author Pieter_Van_Raemdonck - Application Engineers NV/SA - www.ae.be
 * @author Andreas Dangel
 */
public class JavaParser extends JjtreeParserAdapter<ASTCompilationUnit> {

    private final String suppressMarker;
    private final JavaLanguageProcessor javaProcessor;
    private final boolean postProcess;

    public JavaParser(String suppressMarker,
                      JavaLanguageProcessor javaProcessor,
                      boolean postProcess) {
        this.suppressMarker = suppressMarker;
        this.javaProcessor = javaProcessor;
        this.postProcess = postProcess;
    }


    @Override
    protected TokenDocumentBehavior tokenBehavior() {
        return JavaTokenDocumentBehavior.INSTANCE;
    }

    @Override
    protected ASTCompilationUnit parseImpl(CharStream cs, ParserTask task) throws ParseException {

        LanguageVersion version = task.getLanguageVersion();
        int jdkVersion = JavaLanguageProperties.getInternalJdkVersion(version);
        boolean preview = JavaLanguageProperties.isPreviewEnabled(version);

        JavaParserImpl parser = new JavaParserImpl(cs);
        parser.setSuppressMarker(suppressMarker);
        parser.setJdkVersion(jdkVersion);
        parser.setPreview(preview);

        ASTCompilationUnit root = parser.CompilationUnit();
        root.setAstInfo(new AstInfo<>(task, root).withSuppressMap(parser.getSuppressMap()));

        LanguageLevelChecker<?> levelChecker =
            new LanguageLevelChecker<>(jdkVersion,
                                       preview,
                                       // TODO change this strategy with a new lang property
                                       ReportingStrategy.reporterThatThrows());

        levelChecker.check(root);

        if (postProcess) {
            JavaAstProcessor.process(javaProcessor, task.getReporter(), root);
        }

        return root;
    }

    SymbolParser symbolParser() {
        return (task, collector) -> {
            CharStream charStream = CharStream.create(task.getTextDocument(), tokenBehavior());
            // We replace the text document, so that it reflects escapes properly
            // Escapes are processed by CharStream#create
            task = task.withTextDocument(charStream.getTokenDocument().getTextDocument());

            LanguageVersion version = task.getLanguageVersion();
            int jdkVersion = JavaLanguageProperties.getInternalJdkVersion(version);
            boolean preview = JavaLanguageProperties.isPreviewEnabled(version);

            JavaParserImpl parser = new JavaParserImpl(charStream);
            parser.setSkelettonMode(true);
            parser.setJdkVersion(jdkVersion);
            parser.setPreview(preview);

            ASTCompilationUnit root;
            try {

                root = parser.CompilationUnit();
            } catch (FileAnalysisException e) {
                throw e.setFileId(task.getFileId());
            }
            root.setAstInfo(new AstInfo<>(task, root).withSuppressMap(parser.getSuppressMap()));

            JavaAstProcessor.processSkeletton(javaProcessor, task.getReporter(), root);

            // todo use collector

        };
    }
}
