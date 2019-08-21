/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.io.IOException;
import java.io.StringReader;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

public class JavadocLexerAdapter implements TokenManager {

    private final TokenDocument doc;
    private int maxOffset;
    private JavadocLexer lexer;
    private int curOffset;
    private String fname;
    @Nullable
    private JavadocTokenType pendingTok;

    /**
     * @param fileText    Full file text
     * @param startOffset Start offset in the file text
     */
    public JavadocLexerAdapter(String fileText, int startOffset, int endOffset) {
        this.curOffset = startOffset;
        this.doc = new TokenDocument(fileText);
        this.maxOffset = endOffset;
    }

    @Override
    @Nullable
    public JavadocToken getNextToken() {
        if (this.curOffset >= maxOffset) {
            return null;
        }

        try {
            if (lexer == null) {
                StringReader reader = new StringReader(doc.getFullText());
                long skipped = reader.skip(curOffset);
                if (skipped == 0 && curOffset != 0) {
                    return null;
                }

                this.lexer = new JavadocLexer(reader);
            }

            JavadocTokenType tok = pendingTok != null ? pendingTok : lexer.advance();
            pendingTok = null;

            if (tok == JavadocTokenType.COMMENT_END) {
                maxOffset = -1; // stop iteration
            }

            String image;
            int len = lexer.yylength();
            if (tok == null) {
                // EOF
                return null;
            } else if (tok == JavadocTokenType.COMMENT_DATA) {
                // comment data tokens are single chars, we merge them here
                while ((curOffset + len) < maxOffset
                    && (pendingTok = lexer.advance()) == JavadocTokenType.COMMENT_DATA) {
                    len += lexer.yylength();
                }
                image = doc.getFullText().substring(curOffset, curOffset + len);
            } else {
                image = tok.isConst() ? tok.getConstValue() : lexer.yytext();
            }

            int start = curOffset;
            curOffset += len;


            return new JavadocToken(tok, image, start, curOffset, doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFileName(String fileName) {
        this.fname = fileName;
    }

    public String getFname() {
        return fname;
    }
}
