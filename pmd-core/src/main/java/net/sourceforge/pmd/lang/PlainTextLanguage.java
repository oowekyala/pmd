/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.ast.AstInfo;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.ast.Parser.ParserTask;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.document.TextRegion;

/**
 * A dummy language implementation whose parser produces a single node.
 * This is provided for cases where a non-null language is required, but
 * the parser is not useful. This is useful eg to mock rules when no other
 * language is on the classpath. This language is not exposed by {@link LanguageRegistry}
 * and can only be used explicitly with {@link #getInstance()}.
 *
 * @author Clément Fournier
 * @since 6.48.0
 */
@Experimental
public final class PlainTextLanguage extends BaseLanguageModule {

    private static final Language INSTANCE = new PlainTextLanguage();

    static final String TERSE_NAME = "text";

    private PlainTextLanguage() {
        super("Plain text", "Plain text", TERSE_NAME, "plain-text-file-goo-extension");
        addVersion("default", new TextLvh(), true);
    }

    /**
     * Returns the singleton instance of this language.
     */
    public static Language getInstance() {
        return INSTANCE;
    }

    private static final class TextLvh implements LanguageVersionHandler {
        @Override
        public Parser getParser() {
            return PlainTextFile::new;
        }
    }

    /**
     * The only node produced by the parser of {@link PlainTextLanguage}.
     */
    public static class PlainTextFile extends AbstractNode<PlainTextFile, PlainTextFile> implements RootNode {

        private final AstInfo<PlainTextFile> astInfo;


        PlainTextFile(ParserTask task) {
            this.astInfo = new AstInfo<>(task, this);
        }

        @Override
        public TextRegion getTextRegion() {
            return getTextDocument().getEntireRegion();
        }

        @Override
        public String getXPathNodeName() {
            return "TextFile";
        }

        @Override
        public String getImage() {
            return null;
        }

        @Override
        public String toString() {
            return "Plain text file (" + getEndLine() + " lines)";
        }

        @Override
        public AstInfo<? extends RootNode> getAstInfo() {
            return astInfo;
        }
    }

}
