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
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.lang.document.TextRegion;

/**
 * Wraps the generated JFlex lexer into a {@link TokenManager}. The parsers
 * use the additional abstraction {@link TokenCursor}.
 */
class JavadocLexer implements TokenManager<JdocToken> {

    // the flexer produces single chars for those tokens, we merge them here
    private static final Set<JdocTokenType> MERGED_TOKENS =
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
    private @Nullable JdocTokenType pendingTok;

    JavadocLexer(TextDocument commentText) {
        assert commentText.getLanguageVersion().getLanguage() instanceof JavadocLanguage
            : "CommentText language != javadoc: " + commentText.getLanguageVersion();

        this.doc = new JavadocTokenDocument(commentText);
        this.curOffset = 0;
        this.maxOffset = commentText.getLength();
        this.initialState = JavadocFlexer.YYINITIAL;
    }

    /**
     * Produces a lexer that lexes the given token's text. This is used
     * to break up tokens into smaller tokens. Inline tags are lexed
     * initially as a single big token, then, for those that contain
     * references to Java constructs, relexed using the {@link JavadocFlexer#REF_START}
     * state to break it up into smaller tokens.
     *
     * @param token        Token to lex
     * @param initialState Initial parsing state, one of the constants
     *                     defined on {@link JavadocFlexer}
     */
    JavadocLexer(JdocToken token, int initialState) {
        this.doc = token.getDocument();
        this.initialState = initialState;

        TextRegion region = token.getRegion();
        this.curOffset = region.getStartOffset();
        this.maxOffset = region.getEndOffset();
    }

    JavadocTokenDocument getDoc() {
        return doc;
    }

    void replaceLastWith(JdocToken tipOfChain, JdocToken newFirstTok, JdocToken newLastTok) {

        tipOfChain.next = newFirstTok;
        newFirstTok.prev = tipOfChain;

        this.prevToken = newLastTok; // set the end of the chain to the new last token
    }

    /**
     * Note that this never throws {@link TokenMgrError}, because we want to
     * be very resilient to invalid comment source.
     */
    @Override
    @SuppressWarnings("PMD.AssignmentInOperand")
    public @Nullable JdocToken getNextToken() {

        try {
            if (lexer == null) {
                if (this.curOffset >= maxOffset) {
                    return null;
                }
                Chars slice = doc.getFullText().slice(curOffset, maxOffset - curOffset);
                Reader reader = slice.newReader();
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
                StringBuilder imageBuilder = new StringBuilder(lexer.yylength() * 2);
                do {
                    lexer.yyappendtext(imageBuilder);
                } while (curOffset + imageBuilder.length() < maxOffset && (pendingTok = lexer.advance()) == tok);
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
            throw AssertionUtil.shouldNotReachHere(e, "We're reading from an in-memory char slice");
        }
    }

}
