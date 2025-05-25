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
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.INLINE_TAG_END;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.TAG_NAME;
import static net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.WHITESPACE;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdHtmlStart;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlEnd;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocMalformed;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocWhitespace;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink;
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocUnknownInlineTag;

public class JavadocParser {

    private final JavadocLexer lexer;

    private final Deque<AbstractJavadocNode> nodes = new ArrayDeque<>();

    private JavadocToken tok;
    /** End of input. */
    private boolean isEoi;

    public JavadocParser(String fileText, int startOffset, int maxOffset) {
        lexer = new JavadocLexer(fileText, startOffset, maxOffset);
    }

    public JdocComment parse() {
        JdocComment comment = new JdocComment();

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
            linkLeaf(new JdocWhitespace(tok));
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
                AbstractJavadocNode tag = parseInlineTagContent(tok.getImage());
                tag.jjtSetFirstToken(start);
                while (advance() && tok.getKind() != INLINE_TAG_END || tok.getKind() != TAG_NAME) {
                    // advance does the job
                }
                tag.jjtSetLastToken(tokIs(INLINE_TAG_END) ? tok : tok.getPrevious());
                linkLeaf(tag);
            } else if (!isEnd()) {
                growDataLeaf(start, tok);
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

    private AbstractJavadocNode htmlComment() {
        JdocHtmlComment comment = new JdocHtmlComment();
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
            JdocHtmlEnd html = new JdocHtmlEnd(tok.getImage());
            html.jjtSetFirstToken(start);
            advance();
            skipWhitespace();
            if (tokIs(HTML_GT)) {
                html.jjtSetLastToken(tok);
            } else {
                JdocMalformed malformed = new JdocMalformed(EnumSet.of(HTML_GT), tok);
                html.jjtAddChild(malformed, 0);
            }
            return html;
        }
        return new JdocMalformed(EnumSet.of(HTML_IDENT), tok);
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
                JdocMalformed malformed = new JdocMalformed(EnumSet.of(HTML_RCLOSE, HTML_GT), tok);
                html.jjtAddChild(malformed, 0);
            }
            return html;
        }
        return new JdocMalformed(EnumSet.of(HTML_IDENT), tok);
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
        if (lastNode instanceof JdocCommentData) {
            ((JdocCommentData) lastNode).jjtSetLastToken(last);
        } else {
            linkLeaf(new JdocCommentData(first, last));
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

    private void consumeUntil(Predicate<JavadocToken> stopCondition, Predicate<JavadocToken> filter, Consumer<JavadocToken> action) {
        while (stopCondition.test(tok) && advance()) {
            if (filter.test(tok)) {
                action.accept(tok);
            }
        }
    }

    enum KnownInlineTagParser implements TagParser {
        LINK("@link") {
            @Override
            public AbstractJavadocNode parse(String name, JavadocParser parser) {
                parser.skipWhitespace();
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

        @Nullable
        static KnownInlineTagParser lookup(String name) {
            return LOOKUP.get(name);
        }
    }

    interface TagParser {

        String getName();


        AbstractJavadocNode parse(String name, JavadocParser parser);
    }


}
