/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.DOUBLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.EMPTY;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.SINGLE_QUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.UNQUOTED;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_ATTR_VAL;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_COMMENT_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_EQ;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_GT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_IDENT;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_RCLOSE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.HTML_SQUOTE;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.TAG_NAME;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.WHITESPACE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCharacterReference;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlEnd;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLiteral;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocUnknownInlineTag;

public class JavadocParser {

    private final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();

    private TokenCursor<JavadocToken> tokens;

    public JavadocParser(TokenManager<JavadocToken> lexer) {
        this.tokens = new TokenCursor<>(lexer);
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

        while (!nodes.isEmpty()) {
            AbstractJavadocNode top = nodes.pop();
            top.setLastToken(head());
        }
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
        JavadocToken start = head();
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
            return new JdocUnknownInlineTag(name);
        } else {
            return parser.parse(name, this);
        }
    }

    private void htmlStart() {
        JavadocToken start = head();
        advance(); // don't skip whitespace

        if (tokIs(HTML_IDENT)) {
            JdocHtml html = new JdocHtml(head().getImage());
            html.setFirstToken(start);
            advance();

            htmlAttrs(html);

            maybeImplicitClose(start.prev, html.getTagName());
            linkLeaf(html);
            if (tokIs(HTML_RCLOSE) || tokIs(HTML_GT) && html.getBehaviour().isVoid()) {
                html.setAutoclose();
                html.setLastToken(head());
            } else {
                pushNode(html);
                if (!tokIs(HTML_GT)) {
                    linkLeaf(new JdocMalformed(EnumSet.of(HTML_RCLOSE, HTML_GT), head()));
                }
            }
        } else {
            backup(1); // move cursor back to the '<', so that #advance() moves past it
            // FIXME where to report that fucking error?
            linkLeaf(new JdocMalformed(EnumSet.of(HTML_IDENT), head()));
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
    private void maybeImplicitClose(JavadocToken prevEnd, String curTag) {
        if (nodes.peek() instanceof JdocHtml) {

            int i = 0;
            for (AbstractJavadocNode node : new ArrayList<>(nodes)) {
                i++;
                if (node instanceof JdocHtml) {
                    JdocHtml topHtml = (JdocHtml) node;
                    if (topHtml.getBehaviour().shouldCloseBecauseTagIsStarting(curTag)) {
                        popImplicitCloseNodes(i, prevEnd);
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

    private void popImplicitCloseNodes(int n, JavadocToken lastToken) {
        while (n-- > 0) {
            JdocHtml top = (JdocHtml) nodes.pop();
            top.setLastToken(lastToken);
            top.jjtClose();
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
        JavadocToken start = head();
        advance();
        skipWhitespace();
        // </a
        //   ^

        if (tokIs(HTML_IDENT)) {
            JavadocToken ident = head();
            JdocHtmlEnd html = new JdocHtmlEnd(ident.getImage());
            html.setFirstToken(start);
            linkLeaf(html);
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

    private void findNodeToClose(JdocHtmlEnd end, JavadocToken lastToken) {
        int i = 0;
        for (AbstractJavadocNode node : new ArrayList<>(nodes)) {
            i++;
            if (node instanceof JdocHtml) {
                JdocHtml topHtml = (JdocHtml) node;
                if (topHtml.getTagName().equals(end.getTagName())) {
                    popImplicitCloseNodes(i, lastToken);
                    return;
                }
            }
        }
    }

    // TODO error recovery
    private void htmlAttrs(JdocHtml html) {
        // name=
        //    ^
        skipWhitespace();
        while (tokIs(HTML_IDENT)) {
            final JavadocToken name = head();
            final HtmlAttrSyntax syntax;
            final @Nullable JavadocToken value;
            final JavadocToken end;
            @Nullable JdocMalformed malformed = null;

            nextNonWs();

            if (tokIs(HTML_EQ)) {
                // name=
                //     ^
                nextNonWs();
                if (tokIs(JavadocTokenType.ATTR_DELIMITERS)) {
                    // name="
                    //      ^
                    JavadocTokenType firstDelimKind = head().getKind();
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

    private void skipWhitespace() {
        while (head().getKind() == WHITESPACE && advance()) {
            // advance
        }
    }

    private void nextNonWs() {
        advance();
        skipWhitespace();
    }

    private boolean tokIs(JavadocTokenType ttype) {
        return tokens.head() != null && tokens.head().getKind() == ttype;
    }

    private boolean tokIs(EnumSet<JavadocTokenType> ttype) {
        return tokens.head() != null && ttype.contains(tokens.head().getKind());
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

    private void growDataLeaf(JavadocToken first, JavadocToken last) {
        AbstractJavadocNode top = this.nodes.getFirst();
        JavadocNode lastNode = top.jjtGetNumChildren() > 0 ? top.jjtGetChild(top.jjtGetNumChildren() - 1) : null;
        if (lastNode instanceof JdocCommentData) {
            ((JdocCommentData) lastNode).setLastToken(last);
        } else {
            linkLeaf(new JdocCommentData(first, last));
        }
    }

    /** Returns the current token. */
    private JavadocToken head() {
        return tokens.head();
    }

    /** Move the cursor back [n] tokens. */
    private void backup(int n) {
        tokens.backup(n);
    }

    /**
     * Returns false if end of input is reached (in which case tok remains the last non-null token).
     */
    private boolean advance() {
        return tokens.advance();
    }

    /**
     * Consumes token until [stopCondition] is true. All tokens matching
     * the [filter] are fed to the [action]. This method starts by testing
     * the current token.
     */
    private void consumeUntil(Predicate<JavadocToken> stopCondition, Predicate<JavadocToken> filter, Consumer<JavadocToken> action) {
        tokens.consumeUntil(stopCondition, filter, action);
    }



    enum KnownInlineTagParser implements TagParser {
        LINK("@link") {
            @Override
            public AbstractJavadocNode parse(String name, JavadocParser parser) {
                parser.advance();
                StringBuilder builder = new StringBuilder();
                parser.consumeUntil(it -> INLINE_TAG_ENDERS.contains(it.getKind()),
                                    it -> it.getKind().isSignificant(),
                                    tok -> builder.append(tok.getImage()));

                return new JdocLink(name, builder.toString());
            }
        },

        LINKPLAIN("@linkplain") {
            @Override
            public AbstractJavadocNode parse(String name, JavadocParser parser) {
                return LINK.parse(name, parser);
            }
        },

        CODE("@code") {
            @Override
            public AbstractJavadocNode parse(String name, JavadocParser parser) {
                parser.advance();
                StringBuilder builder = new StringBuilder();
                parser.consumeUntil(it -> INLINE_TAG_ENDERS.contains(it.getKind()),
                                    it -> it.getKind().isSignificant(),
                                    tok -> builder.append(tok.getImage()));

                return new JdocLiteral(name, builder.toString());
            }
        },

        LITERAL("@literal") {
            @Override
            public AbstractJavadocNode parse(String name, JavadocParser parser) {
                return CODE.parse(name, parser);
            }
        };

        private static final EnumSet<JavadocTokenType> INLINE_TAG_ENDERS = EnumSet.of(INLINE_TAG_END, TAG_NAME);

        private static final Map<String, KnownInlineTagParser> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(KnownInlineTagParser::getName, p -> p));
        private final String name;

        KnownInlineTagParser(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Nullable // TODO case insensitive lookup
        static KnownInlineTagParser lookup(String name) {
            return LOOKUP.get(name);
        }
    }

    interface TagParser {

        /** Returns the tag name. */
        String getName();


        /**
         * Parse an inline tag. When the method is called, the parser's
         * {@link #head()} is set on the {@link JavadocTokenType#TAG_NAME}.
         *
         * @param name   Name of the tag to parse
         * @param parser Parser
         *
         * @return A node for the tag
         */
        AbstractJavadocNode parse(String name, JavadocParser parser);
    }


}