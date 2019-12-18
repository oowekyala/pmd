/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.DOUBLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.EMPTY;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.SINGLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.UNQUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.COMMENT_DATA;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_GT;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_IDENT;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_RCLOSE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.HTML_SQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.TAG_NAME;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
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
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocRef;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLiteral;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocUnknownInlineTag;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocValue;

class MainJdocParser extends BaseJavadocParser {

    protected final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();
    private final JavadocLexer lexer;

    MainJdocParser(JavadocLexer lexer) {
        super(lexer);
        this.lexer = lexer;
    }

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

    private void inlineTag() {
        JdocToken start = head();
        if (advance()) {
            if (tokIs(TAG_NAME)) {
                AbstractJavadocNode tag = parseInlineTagContent(head().getImage());
                tag.setFirstToken(start);
                tag.setLastToken(tokIs(INLINE_TAG_END) ? head() : head().getPrevious());
                linkLeaf(tag);
            } else if (!tokens.isEoi()) {
                growDataLeaf(start, head());
            }
        }
    }

    /**
     * Parse the content of an inline tag depending on its name. After
     * this exits, we'll consume tokens until the next INLINE_TAG_END,
     * or element that interrupts the tag.
     */
    private AbstractJavadocNode parseInlineTagContent(String name) {
        TagParser parser = KnownInlineTagParser.lookup(name);
        if (parser == null) {
            return KnownInlineTagParser.parseUnknown(name, this);
        } else {
            return parser.parse(name, this);
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
            final HtmlAttrSyntax syntax;
            final @Nullable JdocToken value;
            final JdocToken end;
            @Nullable JdocMalformed malformed = null;

            nextNonWs();

            if (tokIs(HTML_EQ)) {
                // name=
                //     ^
                nextNonWs();
                if (tokIs(JdocTokenType.ATTR_DELIMITERS)) {
                    // name="
                    //      ^
                    JdocTokenType firstDelimKind = head().getKind();
                    syntax = firstDelimKind == HTML_SQUOTE ? SINGLE_QUOTED : DOUBLE_QUOTED;
                    nextNonWs();
                    if (tokIs(HTML_ATTR_VAL)) {
                        // name="value
                        //           ^
                        value = head();
                    } else {
                        // empty value, eg name=""
                        value = null;
                    }
                    nextNonWs();
                    // name="value"
                    //            ^
                    end = head();

                    if (!tokIs(firstDelimKind)) {
                        malformed = new JdocMalformed(EnumSet.of(firstDelimKind), head());
                    }
                } else if (tokIs(HTML_ATTR_VAL)) {
                    // name=value
                    //          ^
                    syntax = UNQUOTED;
                    value = head();
                    end = head();
                } else {
                    // "=", then something that's neither an ident or delimiter
                    syntax = UNQUOTED; // dummy
                    value = null;
                    end = head();
                    malformed = new JdocMalformed(EnumSet.of(HTML_EQ), head());
                }
            } else {
                // tok is then the next token to be processed
                // (eg whitespace or ">")
                syntax = EMPTY;
                value = null;
                end = name;
            }

            JdocHtmlAttr attr = new JdocHtmlAttr(value, syntax);
            attr.setFirstToken(name);
            attr.setLastToken(end);
            if (malformed != null) {
                attr.jjtAddChild(malformed, 0);
            }
            html.addAttribute(attr);
            if (syntax != EMPTY) {
                advance();
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
    private JdocToken parseReference(JdocToken tokBeforeRef, AbstractJavadocNode parent) {
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
                parent.pushChild(ref);
                firstLabelTok = nextNonWs() && tokIs(COMMENT_DATA) ? head() : null;
            }
            // otherwise, link is unparsable
            // doesn't matter, we'll just consume until the closing brace
        }
        return firstLabelTok;
    }

    enum KnownInlineTagParser implements TagParser {
        LINK("@link") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                parser.advance();
                parser.skipWhitespace();
                JdocToken firstTok = parser.head().getPrevious();
                assert firstTok != null;

                JdocLink tag = new JdocLink(name);
                JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
                if (firstLabelTok != null) {
                    while (!parser.tokIs(INLINE_TAG_ENDERS) && parser.advance()) {
                        // skip
                    }
                    JdocCommentData label = new JdocCommentData(firstLabelTok, parser.head().prev);
                    tag.pushChild(label);
                }

                return tag;
            }
        },

        LINKPLAIN("@linkplain") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                return LINK.parse(name, parser);
            }
        },

        CODE("@code") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                parser.advance();
                String data = consumeInlineTag(parser);
                return new JdocLiteral(name, data);
            }
        },

        LITERAL("@literal") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                return CODE.parse(name, parser);
            }
        },

        VALUE("@value") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                parser.advance();
                parser.skipWhitespace();
                JdocToken firstTok = parser.head().getPrevious();
                assert firstTok != null;

                JdocValue tag = new JdocValue(name);
                JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
                if (firstLabelTok != null) {
                    JdocMalformed label = new JdocMalformed(JdocTokenType.EMPTY_SET, firstLabelTok);
                    tag.pushChild(label);
                    while (!parser.tokIs(INLINE_TAG_ENDERS) && parser.advance()) {
                        // skip
                    }
                }
                return tag;
            }
        },
        INHERIT_DOC("@inheritDoc") {
            @Override
            public AbstractJavadocNode parse(String name, MainJdocParser parser) {
                parser.advance();
                parser.skipWhitespace();
                if (parser.tokIs())
                JdocToken firstTok = parser.head().getPrevious();
                assert firstTok != null;

                JdocValue tag = new JdocValue(name);
                JdocToken firstLabelTok = parser.parseReference(firstTok, tag);
                if (firstLabelTok != null) {
                    JdocMalformed label = new JdocMalformed(JdocTokenType.EMPTY_SET, firstLabelTok);
                    tag.pushChild(label);
                    while (!parser.tokIs(INLINE_TAG_ENDERS) && parser.advance()) {
                        // skip
                    }
                }
                return tag;
            }
        }

        // TODO @value
        ;

        private static final EnumSet<JdocTokenType> INLINE_TAG_ENDERS = EnumSet.of(INLINE_TAG_END, TAG_NAME);

        private static final Map<String, KnownInlineTagParser> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(KnownInlineTagParser::getName, p -> p));
        private final String name;

        KnownInlineTagParser(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Nullable // case sensitive
        static KnownInlineTagParser lookup(String name) {
            return LOOKUP.get(name);
        }

        public static JdocUnknownInlineTag parseUnknown(String name, MainJdocParser parser) {
            parser.advance();
            String data = consumeInlineTag(parser);
            return new JdocUnknownInlineTag(name, data);
        }

        @NonNull
        private static String consumeInlineTag(MainJdocParser parser) {
            StringBuilder builder = new StringBuilder();
            parser.consumeUntil(it -> INLINE_TAG_ENDERS.contains(it.getKind()),
                                it -> it.getKind().isSignificant(),
                                tok -> builder.append(tok.getImage()));
            return builder.toString();
        }
    }

    interface TagParser {

        /** Returns the tag name. */
        String getName();


        /**
         * Parse an inline tag. When the method is called, the parser's
         * {@link #head()} is set on the {@link JdocTokenType#TAG_NAME}.
         * When it returns, it should be set on the {@link JdocTokenType#INLINE_TAG_END}.
         *
         * @param name   Name of the tag to parse
         * @param parser Parser
         *
         * @return A node for the tag
         */
        AbstractJavadocNode parse(String name, MainJdocParser parser);
    }


}
