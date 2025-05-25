/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.util.document.TextDocument;

/**
 * Main entry point to parse javadoc comments.
 */
public final class JavadocParserFacade {

    /**
     * Parse the region of the file text enclosed by the start and end
     * as a {@link JdocComment}. The region must start with the token {@code /**}
     * and end with the token {@code * /}.
     *
     * @param document   Text docuemnt
     *
     * @return A Javadoc comment tree
     */
    public static JdocComment parseJavadoc(TextDocument document) {
        final JavadocLexer lexer = new JavadocLexer(document);
        return new MainJdocParser(lexer).parse();
    }

    public static JdocComment parseJavadoc(JavaccToken token) {
        TextDocument baseDocument = token.getDocument().getTextDocument();
        // todo subdocuments
        TextDocument textDocument =
            TextDocument.readOnlyString(
                token.getImage(),
                baseDocument.getDisplayName(),
                baseDocument.getLanguageVersion() // a java version
            );
        return parseJavadoc(textDocument);
    }

}
