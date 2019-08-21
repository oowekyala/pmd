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
        JavadocToken ftoken = lexer.getNextToken();
        if (ftoken.getKind() == JavadocTokenType.COMMENT_START) {
            comment.jjtSetFirstToken(ftoken);
            while (ftoken.getKind() != null && ftoken.getKind() != JavadocTokenType.COMMENT_END) {
                ftoken = lexer.getNextToken();


            }
        }

        return comment;
    }

}
