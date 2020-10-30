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
import java.io.Reader;
import java.util.EnumSet;

import org.apache.commons.io.input.CharSequenceReader;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.util.document.TextDocument;
import net.sourceforge.pmd.util.document.TextRegion;

/**
 * Wraps the generated JFlex lexer into a {@link TokenManager}. The parsers
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


    private final JavadocTokenDocument doc;
    private int maxOffset;
    private JavadocFlexer lexer;
    private final int initialState;
    private int curOffset;
    private JdocToken prevToken;
    private Reader reader;
    @Nullable
    private JdocTokenType pendingTok;

    /**
     * Build a lexer that scans the whole text.
     */
    public JavadocLexer(TextDocument commentText) {
        this(commentText, TextRegion.fromOffsetLength(0, commentText.getLength()));
    }

    /**
     * Builds a lexer that will lex the region of the [fullText] delimited
     * by the given offsets. The region must start with the token "/*". The
     * lexer stops when the region is ended, or when it encounters a "*" "/"
     * token (end of comment), whichever comes first.
     *
     * @param fullText Full file text, may contain Java unicode escapes
     * @param region   Region of the file
     */
    public JavadocLexer(TextDocument fullText, TextRegion region) {
        this(region, new JavadocTokenDocument(fullText), JavadocFlexer.YYINITIAL);
    }

    /**
     * Produces a lexer that lexes the given token's text.
     *
     * @param token        Token to lex
     * @param initialState Initial parsing state, one of the constants
     *                     defined on {@link JavadocFlexer}
     */
    public JavadocLexer(JdocToken token, int initialState) {
        this(token.getRegion(), token.getDocument(), initialState);
    }

    /**
     * Builds a lexer that will lex the region of the [fullText] delimited
     * by the given offsets. The region must start with the token "/*". The
     * lexer stops when the region is ended, or when it encounters a "*" "/"
     * token (end of comment), whichever comes first.
     *
     * @param region       Region to lex in the file text
     * @param document     Token document
     * @param initialState Initial state of the lexer
     */
    public JavadocLexer(TextRegion region, JavadocTokenDocument document, int initialState) {
        this.doc = document;
        this.curOffset = region.getStartOffset();
        this.maxOffset = region.getEndOffset();
        this.initialState = initialState;
    }

    public JavadocTokenDocument getDoc() {
        return doc;
    }

    void replaceLastWith(JdocToken tipOfChain, JdocToken newFirstTok, JdocToken newLastTok) {

        tipOfChain.next = newFirstTok;
        newFirstTok.prev = tipOfChain;

        this.prevToken = newLastTok; // set the end of the chain to the new last token
    }

    @Override
    @SuppressWarnings("PMD.AssignmentInOperand")
    public @Nullable JdocToken getNextToken() {

        try {
            if (lexer == null) {
                if (this.curOffset >= maxOffset) {
                    return null;
                }
                reader = new CharSequenceReader(doc.getFullText());
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
            final int start = curOffset;
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
            end = curOffset;

            JdocToken next = new JdocToken(tok, image, start, end, doc);
            if (prevToken != null) {
                prevToken.next = next;
                next.prev = prevToken;
            }
            prevToken = next;
            return next;
        } catch (IOException e) {
            throw new TokenMgrError(-1, -1, null, "Error lexing Javadoc comment", e);
        }
    }
}
