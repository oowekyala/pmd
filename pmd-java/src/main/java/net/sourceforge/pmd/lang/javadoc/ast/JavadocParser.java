/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


public class JavadocParser {

    private final JavadocLexerAdapter lexer;

    public JavadocParser(String fileText, int startOffset, int maxOffset) {
        lexer = new JavadocLexerAdapter(fileText, startOffset, maxOffset);
    }

    public JavadocComment parse() {
        JavadocComment comment = new JavadocComment();
        JavadocToken ntoken = lexer.getNextToken();
        if (ntoken.getKind() == JavadocTokenType.COMMENT_START) {

        }

        return comment;
    }

}
