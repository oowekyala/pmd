/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.io.IOException;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.util.document.TextDocument;
import net.sourceforge.pmd.util.document.TextFile;

/**
 * Main entry point to parse javadoc comments.
 *
 * <p><b>None of this is published API, and compatibility can be broken anytime!</b>
 * Use this only at your own risk.
 */
@InternalApi
public final class JavadocParserFacade {

    /**
     * Parse the given text document as a javadoc "file". The document must start
     * with the token {@code /**} and end with the token {@code * /} (though this
     * is not enforced, lexing just stops on EOF or the comment end marker).
     *
     * <p>Note that java unicode escapes are not expected to occur in the file,
     * because there is already a preliminary translation phase before we parse
     * java source.
     *
     * @param document Text document for the comment
     *
     * @return A Javadoc comment tree
     */
    public static JdocComment parseJavadoc(TextDocument document) {
        final JavadocLexer lexer = new JavadocLexer(document);
        return new MainJdocParser(lexer).parse();
    }

    /**
     * Parse a <i>Java</i> token corresponding to a javadoc comment as if
     * with {@link #parseJavadoc(TextDocument)}.
     */
    public static JdocComment parseJavaToken(JavaccToken token) {
        TextDocument baseDocument = token.getDocument().getTextDocument();
        // todo subdocuments
        try {
            TextDocument textDocument = TextDocument.create(
                TextFile.forReader(
                    baseDocument.sliceTranslatedText(token.getRegion()).newReader(),
                    baseDocument.getDisplayName(),
                    baseDocument.getLanguageVersion() // a java version
                ).build()
            );
            return parseJavadoc(textDocument);
        } catch (IOException e) {
            throw AssertionUtil.shouldNotReachHere(e.getMessage());
        }
    }

}
