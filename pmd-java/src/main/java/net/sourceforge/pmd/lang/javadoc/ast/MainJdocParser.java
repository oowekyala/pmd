/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.DOUBLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.EMPTY;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.SINGLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.UNQUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.ATTR_DELIMITERS;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.COMMENT_DATA;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.EXPECTED_TOKEN;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_DQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_GT;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_IDENT;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_RCLOSE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_SQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.TAG_NAME;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCharacterReference;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml.HtmlCloseSyntax;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlEnd;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;

class MainJdocParser extends BaseJavadocParser {

    protected final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();
    private final JavadocLexer lexer;

    MainJdocParser(JavadocLexer lexer) {
        super(lexer);
        this.lexer = lexer;
    }

    /**
     * Root production.
     */
    public JdocComment parse() {
        JdocComment comment = new JdocComment();

        advance();
        if (head() == null) {
            // EOF
            return null;
        }
        comment.setFirstToken(head());

        pushNode(comment);

        while (advance()) {
            dispatch();
        }

        finishStack(head());
        return comment;
    }

    private void dispatch() {
        switch (head().getKind()) {
        case COMMENT_END:
        case WHITESPACE:
        case LINE_BREAK:
            return;
        case COMMENT_DATA:
            growDataLeaf(head(), head());
            break;
        case INLINE_TAG_START:
            inlineTag();
            break;
        case HTML_LT:
            htmlStart();
            break;
        case HTML_LCLOSE:
            htmlEnd();
            break;
        case HTML_COMMENT_START:
            linkLeaf(htmlComment());
            break;
        case CHARACTER_REFERENCE:
            linkLeaf(new JdocCharacterReference(head()));
            break;
        }
    }

    private void blockTag() {
        JdocToken tagname = head();
        assert tagname.getKind() == TAG_NAME;
        JdocBlockTag tag = KnownBlockTagParser.selectAndParse(head().getImage(), this);
        tag.setFirstToken(tagname);

        if (nodes.peek() instanceof JdocBlockTag) {
            // close previous node
            AbstractJavadocNode pop = nodes.pop();

        }


    }

    private void inlineTag() {
        JdocToken start = head();
        if (advance()) {
            if (tokIs(TAG_NAME)) {
                AbstractJavadocNode tag = KnownInlineTagParser.selectAndParse(head().getImage(), this);
                tag.setFirstToken(start);
                if (tokIs(INLINE_TAG_END)) {
                    tag.setLastToken(head());
                } else {
                    tag.setLastToken(head().getPrevious());
                    JdocToken error = JdocToken.implicitBefore(EXPECTED_TOKEN, head());
                    tag.appendChild(new JdocMalformed(EnumSet.of(INLINE_TAG_END), error));
                }
                linkLeaf(tag);
            } else if (!tokens.isEoi()) {
                growDataLeaf(start, head());
            }
        }
    }

    private void htmlStart() {
        JdocToken start = head();
        advance(); // don't skip whitespace

        assert tokIs(HTML_IDENT); // lexer doesn't push an HTML_LT if there is no following ident

        JdocHtml html = new JdocHtml(head().getImage());
        html.setFirstToken(start);
        advance();

        htmlAttrs(html);

        maybeImplicitClose(start.prev, html.getTagName());
        linkLeaf(html);
        if (tokIs(HTML_RCLOSE)) {
            html.setCloseSyntax(HtmlCloseSyntax.XML);
            html.setLastToken(head());
        } else if (tokIs(HTML_GT) && html.getBehaviour().isVoid()) {
            html.setCloseSyntax(HtmlCloseSyntax.VOID);
            html.setLastToken(head());
        } else {
            pushNode(html);
            if (!tokIs(HTML_GT)) {
                linkLeaf(new JdocMalformed(EnumSet.of(HTML_GT), head()));
            }
        }
    }

    /**
     * Autoclose current HTML nodes if applicable.
     * Some nodes higher up in the stack may be autoclosed, and cause
     * their children to be autoclosed as well.
     *
     * @param prevEnd Token that should be used as the end token of the autoclosed tag
     * @param curTag  Name of the tag being opened
     */
    private void maybeImplicitClose(JdocToken prevEnd, String curTag) {
        if (nodes.peek() instanceof JdocHtml) {

            int i = 0;
            for (AbstractJavadocNode node : new ArrayList<>(nodes)) {
                i++;
                if (node instanceof JdocHtml) {
                    JdocHtml topHtml = (JdocHtml) node;
                    if (topHtml.getBehaviour().shouldCloseBecauseTagIsStarting(curTag)) {
                        popHtmlUntil(i, prevEnd, false);
                        i = 0;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * @param n              Number of nodes to pop from the stack, all are JdocHtml
     * @param lastToken      Last token to set to all the popped nodes
     * @param lastIsExplicit If true, the last node popped is closed by an explicit end tag,
     *                       otherwise they're either implicit or unclosed
     */
    private void popHtmlUntil(int n, JdocToken lastToken, boolean lastIsExplicit) {
        JdocHtml last = null;
        while (n-- > 0) {
            JdocHtml top = (JdocHtml) nodes.pop();
            top.setCloseSyntax(HtmlCloseSyntax.IMPLICIT);
            if (last != null && !last.getBehaviour().shouldCloseBecauseParentIsEnded(top.getTagName())) {
                last.setCloseSyntax(HtmlCloseSyntax.UNCLOSED);
            }
            top.setLastToken(lastToken);
            top.jjtClose();
            last = top;
        }
        if (lastIsExplicit && last != null) { // this was closed normally
            last.setCloseSyntax(HtmlCloseSyntax.HTML);
        }
    }

    private void finishStack(JdocToken lastToken) {
        JdocHtml html = null;
        while (!nodes.isEmpty()) {
            AbstractJavadocNode top = nodes.pop();
            if (top instanceof JdocHtml) {
                html = (JdocHtml) top;
                html.setCloseSyntax(HtmlCloseSyntax.IMPLICIT);
            }
            top.setLastToken(lastToken);
            top.jjtClose();
        }

        // the last HTML node, unclosed
        if (html != null) {
            boolean implicitClose = html.getBehaviour().shouldCloseBecauseParentIsEnded("div"); // the name here is irrelevant
            html.setCloseSyntax(implicitClose ? HtmlCloseSyntax.IMPLICIT : HtmlCloseSyntax.UNCLOSED);
        }
    }

    private AbstractJavadocNode htmlComment() {
        JdocHtmlComment comment = new JdocHtmlComment();
        comment.setFirstToken(head());
        while (advance() && !tokIs(HTML_COMMENT_END)) {
            comment.setLastToken(head());
        }
        return comment;
    }

    private void htmlEnd() {
        // </
        //  ^
        JdocToken start = head();
        advance();
        skipWhitespace();
        // </a
        //   ^

        if (tokIs(HTML_IDENT)) {
            JdocToken ident = head();
            JdocHtmlEnd html = new JdocHtmlEnd(ident.getImage());
            html.setFirstToken(start);
            advance();
            skipWhitespace();
            // </a>
            //    ^

            html.setLastToken(head());
            if (!tokIs(HTML_GT)) {
                html.appendChild(new JdocMalformed(EnumSet.of(HTML_GT), head()));
            }
            findNodeToClose(html, tokIs(HTML_GT) ? head() : ident);
            return;
        }
        linkLeaf(new JdocMalformed(EnumSet.of(HTML_IDENT), head()));
    }

    private void findNodeToClose(JdocHtmlEnd end, JdocToken lastToken) {
        int i = 0;
        for (AbstractJavadocNode node : new ArrayList<>(nodes)) {
            i++;
            if (node instanceof JdocHtml) {
                JdocHtml topHtml = (JdocHtml) node;
                if (topHtml.getTagName().equalsIgnoreCase(end.getTagName())) {
                    topHtml.appendChild(end);
                    popHtmlUntil(i, lastToken, true);
                    return;
                }
            }
        }
        // no node to close, the end tag is dangling
        linkLeaf(end);
    }

    private void htmlAttrs(JdocHtml html) {
        // name=
        //    ^
        skipWhitespace();
        while (tokIs(HTML_IDENT)) {
            final JdocToken name = head();

            nextNonWs();

            if (tokIs(HTML_EQ)) {
                // name=
                //     ^
                nextNonWs();
                if (tokIsAny(ATTR_DELIMITERS)) {
                    // name="
                    //      ^
                    JdocTokenType firstDelimKind = head().getKind();
                    HtmlAttrSyntax syntax = firstDelimKind == HTML_SQUOTE ? SINGLE_QUOTED : DOUBLE_QUOTED;
                    nextNonWs();
                    final @Nullable JdocToken value;
                    if (tokIs(HTML_ATTR_VAL)) {
                        // name="value
                        //           ^
                        value = head();
                        nextNonWs();
                    } else {
                        // empty value, eg name=""
                        nextNonWs();
                        value = JdocToken.implicitBefore(HTML_ATTR_VAL, head());
                    }
                    // name="value"
                    //            ^
                    if (!tokIs(firstDelimKind)) {
                        JdocHtmlAttr attr = html.newAttribute(name, value, value, syntax);
                        attr.newError(EnumSet.of(firstDelimKind), head());
                    } else {
                        html.newAttribute(name, value, head(), syntax);
                    }
                } else if (tokIs(HTML_ATTR_VAL)) {
                    // name=value
                    //          ^
                    html.newAttribute(name, head(), head(), UNQUOTED);
                } else {
                    // "=", then something that's neither an ident or delimiter
                    html.newError(EnumSet.of(HTML_ATTR_VAL, HTML_SQUOTE, HTML_DQUOTE), head());
                }
                advance();
            } else {
                // tok is then the next token to be processed
                // (eg whitespace or ">")
                html.newAttribute(name, null, name, EMPTY);
            }

            skipWhitespace();
        }
    }

    private void pushNode(AbstractJavadocNode node) {
        nodes.push(node);
    }

    private void linkLeaf(AbstractJavadocNode node) {
        if (node == null) {
            return;
        }
        AbstractJavadocNode top = this.nodes.peek();
        Objects.requireNonNull(top).jjtAddChild(node, top.jjtGetNumChildren());
    }

    private void growDataLeaf(JdocToken first, JdocToken last) {
        AbstractJavadocNode top = this.nodes.getFirst();
        JavadocNode lastNode = top.jjtGetNumChildren() > 0 ? top.jjtGetChild(top.jjtGetNumChildren() - 1) : null;
        if (lastNode instanceof JdocCommentData) {
            ((JdocCommentData) lastNode).setLastToken(last);
        } else {
            linkLeaf(new JdocCommentData(first, last));
        }
    }

    /**
     * Parse a class/field/method reference from the head(). The head
     * must be a {@link JdocTokenType#COMMENT_DATA}, it will be split
     * into more tokens and those will be reincorporated into the token
     * stream.
     *
     * <p>If this succeeds this adds a {@link JdocRef} child to the [parent],
     * and returns the first comment-data node after the reference (the rest
     * of the head after removing the reference).
     *
     * @param tokBeforeRef Token to be linked before the ref
     * @param parent       Parent on which the reference should be set
     *
     * @return First {@link JdocTokenType#COMMENT_DATA} token after the reference,
     *     or null if there is none
     */
    @Nullable
    JdocToken parseReference(JdocToken tokBeforeRef, AbstractJavadocNode parent) {
        JdocToken firstLabelTok = null;
        if (tokIs(COMMENT_DATA)) {
            JdocToken cdata = head();

            JdocRefParser refParser = new JdocRefParser(cdata);
            JdocRef ref = refParser.parse();
            if (ref != null) {
                assert ref.getFirstToken() != null : "RefParser should have set first token on " + ref;
                assert ref.getLastToken() != null : "RefParser should have set last token on " + ref;
                lexer.replaceLastWith(tokBeforeRef, ref.getFirstToken(), ref.getLastToken());
                tokens.reset(ref.getLastToken());
                parent.appendChild(ref);
                firstLabelTok = nextNonWs() && tokIs(COMMENT_DATA) ? head() : null;
            }
            // otherwise, link is unparsable
            // doesn't matter, we'll just consume until the closing brace
        }
        return firstLabelTok;
    }


}
