/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.BAD_CHAR;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_DATA;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_COMMENT_CONTENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.EnumSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/**
 * Wraps the generated JFlex lexer.
 */
class JavadocLexer implements TokenManager<JavadocToken> {

    // the flexer produces single chars for those tokens, we merge them here
    private static final EnumSet<JavadocTokenType> MERGED_TOKENS =
        EnumSet.of(
            COMMENT_DATA,
            HTML_COMMENT_CONTENT,
            HTML_ATTR_VAL,
            BAD_CHAR
        );


    private final TokenDocument<JavadocToken> doc;
    private int maxOffset;
    private net.sourceforge.pmd.lang.javadoc.ast.JavadocFlexer lexer;
    private JavadocToken prevToken;
    private int curOffset;
    @Nullable
    private JavadocTokenType pendingTok;

    /**
     * Build a lexer that scans the whole text.
     *
     * @see #JavadocLexer(String, int, int)
     */
    public JavadocLexer(String fullText) {
        this(fullText, 0, fullText.length());
    }

    /**
     * Builds a lexer that will lex the region of the [fullText] delimited
     * by the given offsets. The region must start with the token "/*". The
     * lexer stops when the region is ended, or when it encounters a "*" "/"
     * token (end of comment), whichever comes first.
     *
     * @param fullText    Full file text, may contain Java unicode escapes
     * @param startOffset Start offset in the file text
     * @param endOffset   End offset (exclusive) in the file text
     */
    public JavadocLexer(String fullText, int startOffset, int endOffset) {
        this.doc = new JavadocTokenDocument(fullText);
        this.curOffset = startOffset;
        this.maxOffset = endOffset;
    }

    public TokenDocument<JavadocToken> getDoc() {
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

                this.lexer = new net.sourceforge.pmd.lang.javadoc.ast.JavadocFlexer(reader);
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
            } else if (MERGED_TOKENS.contains(tok)) {
                // adjacent tokens of those kinds are merged together
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
            throw new RuntimeException(e);// TODO
        }
    }
}
