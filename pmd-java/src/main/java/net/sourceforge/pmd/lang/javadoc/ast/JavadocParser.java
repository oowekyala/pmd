/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_START;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_GT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_IDENT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_RCLOSE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.TAG_NAME;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.WHITESPACE;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Objects;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdHtmlComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdHtmlEnd;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdHtmlStart;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdInlineTag;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdWhitespace;

public class JavadocParser {

    private final JavadocLexerAdapter lexer;

    private final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();

    private JavadocToken tok;
    /** End of input. */
    private boolean isEoi;

    public JavadocParser(String fileText, int startOffset, int maxOffset) {
        lexer = new JavadocLexerAdapter(fileText, startOffset, maxOffset);
    }

    public JavadocNode.JdComment parse() {
        JavadocNode.JdComment comment = new JavadocNode.JdComment();

        advance();
        if (tok == null) {
            // EOF
            return null;
        }
        comment.jjtSetFirstToken(tok);

        while (advance()) {
            dispatch();
        }

        comment.jjtSetLastToken(tok);
        return comment;
    }

    private void dispatch() {
        switch (tok.getKind()) {
        case COMMENT_END:
            break;
        case COMMENT_DATA:
            growDataLeaf(tok, tok);
            break;
        case WHITESPACE:
            linkLeaf(new JdWhitespace(tok));
            break;
        case INLINE_TAG_START:
            inlineTag();
            break;
        case HTML_LT:
            linkLeaf(htmlStart());
            break;
        case HTML_LCLOSE:
            linkLeaf(htmlEnd());
            break;
        case HTML_COMMENT_START:
            linkLeaf(htmlComment());
            break;
        }
    }

    private void inlineTag() {
        JavadocToken start = tok;
        if (advance()) {
            if (tokIs(TAG_NAME)) {
                JdInlineTag tag = new JdInlineTag(tok.getImage());
                tag.jjtSetFirstToken(start);
                parseTagContent(tag);
                tag.jjtSetLastToken(tok);
                linkLeaf(tag);
            } else if (!isEnd()) {
                growDataLeaf(start, tok);
            }
        }
    }

    private void parseTagContent(JdInlineTag tag) {
        // TODO parse depending on tag name
    }

    private AbstractJavadocNode htmlComment() {
        JdHtmlComment comment = new JdHtmlComment();
        comment.jjtSetFirstToken(tok);
        while (advance() && !tokIs(HTML_COMMENT_END)) {
            comment.jjtSetLastToken(tok);
        }
        return comment;
    }

    private AbstractJavadocNode htmlEnd() {
        JavadocToken start = tok;
        advance();
        skipWhitespace();
        if (tokIs(HTML_IDENT)) {
            JdHtmlEnd html = new JdHtmlEnd(tok.getImage());
            html.jjtSetFirstToken(start);
            advance();
            skipWhitespace();
            if (tokIs(HTML_GT)) {
                html.jjtSetLastToken(tok);
            } else {
                JdMalformed malformed = new JdMalformed(EnumSet.of(HTML_GT), tok);
                html.jjtAddChild(malformed, 0);
            }
            return html;
        }
        return new JdMalformed(EnumSet.of(HTML_IDENT), tok);
    }

    private AbstractJavadocNode htmlStart() {
        JavadocToken start = tok;
        advance();
        skipWhitespace();

        if (tokIs(HTML_IDENT)) {
            JdHtmlStart html = new JdHtmlStart(tok.getImage());
            html.jjtSetFirstToken(start);
            htmlAttrs(html);
            switch (tok.getKind()) {
            case HTML_RCLOSE:
                html.setAutoclose(true);
                // fallthrough
            case HTML_GT:
                html.jjtSetLastToken(tok);
                break;
            default:
                JdMalformed malformed = new JdMalformed(EnumSet.of(HTML_RCLOSE, HTML_GT), tok);
                html.jjtAddChild(malformed, 0);
            }
            return html;
        }
        return new JdMalformed(EnumSet.of(HTML_IDENT), tok);
    }

    private void htmlAttrs(JdHtmlStart acc) {
        skipWhitespace();
        while (tokIs(HTML_IDENT)) {
            String name = tok.getImage();
            advance();
            skipWhitespace();
            if (tokIs(HTML_EQ)) {
                advance();

                if (tokIs(HTML_ATTR_START)) {
                    advance();
                    if (tokIs(HTML_ATTR_VAL)) {
                        acc.attributes.put(name, tok.getImage());
                    }
                    advance();
                    skipWhitespace();
                }
            } else {
                acc.attributes.put(name, JdHtmlStart.UNATTRIBUTED);
                advance();
                skipWhitespace();
            }
        }
    }

    private void skipWhitespace() {
        while (tok.getKind() == WHITESPACE && advance()) {
            // advance
        }
    }

    private boolean tokIs(JavadocTokenType ttype) {
        return tok != null && tok.getKind() == ttype;
    }

    private boolean isEnd() {
        return isEoi;
    }

    private void linkLeaf(AbstractJavadocNode node) {
        if (node == null) {
            return;
        }
        AbstractJavadocNode top = this.nodes.peek();
        Objects.requireNonNull(top).jjtAddChild(node, top.jjtGetNumChildren());
    }

    private void growDataLeaf(JavadocToken first, JavadocToken last) {
        AbstractJavadocNode top = this.nodes.getFirst();
        JavadocNode lastNode = top.jjtGetNumChildren() > 0 ? top.jjtGetChild(top.jjtGetNumChildren() - 1) : null;
        if (lastNode instanceof JdCommentData) {
            ((JdCommentData) lastNode).jjtSetLastToken(last);
        } else {
            linkLeaf(new JdCommentData(first, last));
        }
    }

    /**
     * Returns false if end of input is reached (in which case tok remains the last non-null token).
     */
    private boolean advance() {
        if (isEoi) {
            return false;
        }
        JavadocToken t = lexer.getNextToken();
        if (t == null) {
            isEoi = true;
            return false;
        }
        tok = t;
        return true;
    }


}
