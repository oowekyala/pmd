/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.AstInfo;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccTokenDocument.TokenDocumentBehavior;
import net.sourceforge.pmd.lang.ast.impl.javacc.JjtreeParserAdapter;
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
    private final boolean skelettonMode;

    public JavaParser(String suppressMarker,
                      JavaLanguageProcessor javaProcessor,
                      boolean postProcess) {
        this(suppressMarker, javaProcessor, postProcess, false);
    }

    private JavaParser(String suppressMarker,
                       JavaLanguageProcessor javaProcessor,
                       boolean postProcess,
                       boolean skelettonMode) {
        this.suppressMarker = suppressMarker;
        this.javaProcessor = javaProcessor;
        this.postProcess = postProcess;
        this.skelettonMode = skelettonMode;
    }


    /**
     * Parse the symbols of this file and accumulate them into the given
     * collector. Compared to regular parsing, this parser
     * <ul>
     * <li>Does not read code blocks, only the signatures of methods and fields and classes
     * <li>Does not perform type resolution or disambiguation (symbols are incomplete).
     * </ul>
     * Disambiguation requires information about the types of the whole project.
     * This information is collected by this pass. This pass only collects the names
     * of symbols and their signatures and performs disambiguation and linking of
     * symbols together later on.
     */
    public static JavaParser getSkelettonParser(JavaLanguageProcessor processor) {
        return new JavaParser("NOPMD", processor, true, true);
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
        parser.setSkelettonMode(skelettonMode);

        ASTCompilationUnit root = parser.CompilationUnit();
        root.setAstInfo(new AstInfo<>(task, root).withSuppressMap(parser.getSuppressMap()));

        if (!skelettonMode) {
            LanguageLevelChecker<?> levelChecker =
                new LanguageLevelChecker<>(jdkVersion,
                                           preview,
                                           // TODO change this strategy with a new lang property
                                           ReportingStrategy.reporterThatThrows());

            levelChecker.check(root);
        }

        if (postProcess) {
            if (skelettonMode) {
                JavaAstProcessor.processSkeletton(javaProcessor, task.getReporter(), root);
            } else {
                JavaAstProcessor.process(javaProcessor, task.getReporter(), root);
            }
        }

        return root;
    }

}
