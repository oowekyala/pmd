/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.BAD_CHAR;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.COMMENT_DATA;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_COMMENT_CONTENT;

import java.io.IOException;
import java.util.EnumSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.lang.ast.impl.JavaInputReader;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/**
 * Wraps the generated JFlex lexer into a {@link TokenStream}. The parsers
 * use the additional abstraction {@link TokenCursor}.
 */
class JavadocLexer implements TokenManager<JdocToken> {

    // the flexer produces single chars for those tokens, we merge them here
    private static final EnumSet<JdocTokenType> MERGED_TOKENS =
        EnumSet.of(
            COMMENT_DATA,
            HTML_COMMENT_CONTENT,
            HTML_ATTR_VAL,
            BAD_CHAR
        );


    private final TokenDocument<JavadocToken> doc;
    private int maxOffset;
    private JavadocFlexer lexer;
    private final int initialState;
    private int curOffset;
    private JdocToken prevToken;
    private JavaInputReader reader;
    @Nullable
    private JdocTokenType pendingTok;

    /**
     * Build a lexer that scans the whole text.
     *
     * @see #JavadocLexer(String, int, int)
     */
    public JavadocLexer(String commentText) {
        this(commentText, 0, commentText.length());
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
        this(startOffset, endOffset, new JavadocTokenDocument(fullText), JavadocFlexer.YYINITIAL);
    }

    /**
     * Produces a lexer that lexes the given token's text.
     *
     * @param token        Token to lex
     * @param initialState Initial parsing state, one of the constants
     *                     defined on {@link JavadocFlexer}
     */
    public JavadocLexer(JdocToken token, int initialState) {
        this(token.getStartInDocument(), token.getEndInDocument(), token.getDocument(), initialState);
    }

    /**
     * Builds a lexer that will lex the region of the [fullText] delimited
     * by the given offsets. The region must start with the token "/*". The
     * lexer stops when the region is ended, or when it encounters a "*" "/"
     * token (end of comment), whichever comes first.
     *
     * @param startOffset  Start offset in the file text
     * @param endOffset    End offset (exclusive) in the file text
     * @param document     Token document
     * @param initialState Initial state of the lexer
     */
    public JavadocLexer(int startOffset, int endOffset, TokenDocument<JavadocToken> document, int initialState) {
        this.doc = document;
        this.curOffset = startOffset;
        this.maxOffset = endOffset;
        this.initialState = initialState;
    }

    public TokenDocument<JavadocToken> getDoc() {
        return doc;
    }

    @Override
    public boolean isEof(@NonNull JdocToken token) {
        return token == null;
    }

    void replaceLastWith(JdocToken tipOfChain, JdocToken newFirstTok, JdocToken newLastTok) {

        tipOfChain.next = newFirstTok;
        newFirstTok.prev = tipOfChain;

        this.prevToken = newLastTok; // set the end of the chain to the new last token
    }

    @Override
    @Nullable
    @SuppressWarnings("PMD.AssignmentInOperand")
    public JdocToken getNextToken() {

        try {
            if (lexer == null) {
                if (this.curOffset >= maxOffset) {
                    return null;
                }
                reader = new JavaInputReader(doc.getFullText(), curOffset, maxOffset);
                this.lexer = new JavadocFlexer(reader);
                lexer.yybegin(initialState);
            }

            if (prevToken != null && prevToken.next != null) {
                // do this before checking for curOffset
                prevToken = prevToken.next;
                return prevToken;
            }

            if (this.curOffset >= maxOffset) {
                return null;
            }

            final JdocTokenType tok = pendingTok != null ? pendingTok : lexer.advance();
            pendingTok = null;

            if (tok == COMMENT_END) {
                maxOffset = -1; // stop iteration
            }

            // in the image, unicode escapes are translated
            final String image;
            final int start = reader.inputOffset(curOffset);
            final int end;
            if (tok == null) {
                // EOF
                return null;
            } else if (MERGED_TOKENS.contains(tok)) {
                // adjacent tokens of those kinds are merged together
                StringBuilder imageBuilder = new StringBuilder();
                int len = 0;
                do {
                    int yylength = lexer.yylength();
                    for (int i = 0; i < yylength; i++) {
                        imageBuilder.append(lexer.yycharat(i));
                    }
                    len += yylength;
                } while (curOffset + len < maxOffset && (pendingTok = lexer.advance()) == tok);
                image = imageBuilder.toString();
            } else {
                image = tok.isConst() ? tok.getConstValue() : lexer.yytext();
            }

            curOffset += image.length();
            end = reader.inputOffset(curOffset);

            JdocToken next = new JdocToken(tok, image, start, end, doc);
            if (prevToken != null) {
                prevToken.next = next;
                next.prev = prevToken;
            }
            prevToken = next;
            return next;
        } catch (IOException e) {
            throw new TokenMgrError("Error lexing Javadoc comment", e);
        }
    }
}
