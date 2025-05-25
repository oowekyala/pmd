/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.ATTR_DELIMITERS;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_DATA;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_DQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_SQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.WHITESPACE;

import java.io.IOException;
import java.io.StringReader;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

public class JavadocLexer implements TokenManager<JavadocToken> {

    private final TokenDocument<JavadocToken> doc;
    private int maxOffset;
    private JavadocFlexer lexer;
    private JavadocToken prevToken;
    private int curOffset;
    @Nullable
    private JavadocTokenType pendingTok;

    public JavadocLexer(String fullText) {
        this(fullText, 0, fullText.length());
    }

    /**
     * @param fileText    Full file text
     * @param startOffset Start offset in the file text
     */
    public JavadocLexer(String fileText, int startOffset, int endOffset) {
        this.doc = new JavadocTokenDocument(fileText);
        this.curOffset = startOffset;
        this.maxOffset = endOffset;
    }

    public TokenDocument getDoc() {
        return doc;
    }

    @Override
    @Nullable
    @SuppressWarnings("PMD.AssignmentInOperand")
    public JavadocToken getNextToken() {
        if (this.curOffset >= maxOffset) {
            return null;
        }

        try {
            if (lexer == null) {
                StringReader reader = new StringReader(doc.getFullText()); // TODO java unicode escapes
                long skipped = reader.skip(curOffset);
                if (skipped == 0 && curOffset != 0) {
                    return null;
                }

                this.lexer = new JavadocFlexer(reader);
            }

            final JavadocTokenType tok = pendingTok != null ? pendingTok : lexer.advance();
            pendingTok = null;

            if (tok == COMMENT_END) {
                maxOffset = -1; // stop iteration
            }

            final String image;
            int len = lexer.yylength();
            if (tok == null) {
                // EOF
                return null;
            } else if (JavadocTokenType.MERGED_TOKENS.contains(tok)) {
                // those tokens are single chars, we merge them here
                while ((curOffset + len) < maxOffset
                    && (pendingTok = lexer.advance()) == tok) {
                    len += lexer.yylength();
                }
                image = doc.getFullText().substring(curOffset, curOffset + len);
            } else {
                image = tok.isConst() ? tok.getConstValue() : lexer.yytext();
            }

            final int start = curOffset;
            curOffset += len;


            JavadocToken next = new JavadocToken(tok, image, start, curOffset, doc);
            if (prevToken != null) {
                prevToken.next = next;
                next.prev = prevToken;
            }
            prevToken = next;
            return next;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
