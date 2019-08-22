/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.COMMENT_START;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_START;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_GT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_IDENT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_RCLOSE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.TAG_NAME;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.WHITESPACE;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

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

    public JavadocParser(String fileText, int startOffset, int maxOffset) {
        lexer = new JavadocLexerAdapter(fileText, startOffset, maxOffset);
    }

    public JavadocNode.JdComment parse() {
        JavadocNode.JdComment comment = new JavadocNode.JdComment();
        JavadocToken tok = lexer.getNextToken();
        if (tok == null) {
            return null;
        }
        @NonNull JavadocToken prev = tok;
        if (tok.getKind() == COMMENT_START) {
            comment.jjtSetFirstToken(tok);

            while ((tok = lexer.getNextToken()) != null) {
                prev = tok;

                switch (tok.getKind()) {
                case COMMENT_END:
                    // TODO unwind stack
                    comment.jjtSetLastToken(tok);
                    return comment;
                case COMMENT_DATA:
                    growDataLeaf(tok, tok);
                    break;
                case WHITESPACE:
                    linkLeaf(new JdWhitespace(tok));
                    break;
                case INLINE_TAG_START:
                    AbstractJavadocNode node = inlineTag();
                    if (node != null) {
                        linkLeaf(node);
                    }
                    break;
                case HTML_LT:
                    linkLeaf(html());
                    break;
                case HTML_LCLOSE:
                    linkLeaf(htmlEnd());
                    break;
                case HTML_COMMENT_START:
                    linkLeaf(htmlComment());
                    break;
                }
            }
            comment.jjtSetLastToken(prev);
            return comment;
        }
        return null;
    }

    private AbstractJavadocNode inlineTag() {
        JavadocToken start = tok;
        advance();

        if (tokIs(TAG_NAME)) {
            JdInlineTag tag = new JdInlineTag(tok.getImage());
            tag.jjtSetFirstToken(start);
            parseTagContent(tag);
            if (tokIs(INLINE_TAG_END)) {
                // else what
                tag.jjtSetLastToken(tok);
            }
            return tag;
        } else {
            growDataLeaf(start, tok);
            return null;
        }
    }

    private void parseTagContent(JdInlineTag tag) {
        // TODO
    }

    private AbstractJavadocNode htmlComment() {
        JdHtmlComment comment = new JdHtmlComment();
        comment.jjtSetFirstToken(tok);
        advance();
        while (tok != null && !tokIs(HTML_COMMENT_END)) {
            comment.jjtSetLastToken(tok);
            advance();
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

    private AbstractJavadocNode html() {
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
                skipWhitespace();
            }
        }
    }

    private void skipWhitespace() {
        while (tok != null && tok.getKind() == WHITESPACE) {
            advance();
        }
    }

    private boolean tokIs(JavadocTokenType ttype) {
        return tok != null && tok.getKind() == ttype;
    }

    private boolean isEnd(JavadocToken next) {
        return next == null || next.getKind() == COMMENT_END;
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


    private void advance() {
        tok = lexer.getNextToken();
    }


}
