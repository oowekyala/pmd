/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.internal.util.IteratorUtil;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;
import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax;
import net.sourceforge.pmd.util.document.Chars;
import net.sourceforge.pmd.util.document.TextDocument;


/**
 * A node for the Javadoc language. The javadoc AST is pretty simple:
 * <ul>
 *     <li>The root node is always {@link JdocComment}.
 *     <li>Significant text of the comment is represented by {@link JdocCommentData}.
 *     <li>Javadoc inline tags (eg {@code {@code foo}}, are represented by {@link JdocInlineTag}
 *     and its subclasses.
 *     <li>References to Java members are represented by {@link JdocRef}
 *     and its subclasses.
 *     <li>HTML nodes are represented by {@link JdocHtml}.
 *     <li>HTML comments are represented by {@link JdocHtmlComment}.
 *     <li>Malformed HTML is represented by {@link JdocMalformed}.
 *     <li>Whitespace characters, and leading asterisks are only available
 *     in the tokens (see {@link JdocTokenType#LINE_BREAK} and {@link JdocTokenType#WHITESPACE}).
 * </ul>
 */
public interface JavadocNode extends TextAvailableNode, GenericNode<JavadocNode> {


    JdocToken getFirstToken();


    JdocToken getLastToken();


    /**
     * Returns true if this node is implied by context.
     */
    default boolean isImplicit() {
        return getFirstToken().equals(getLastToken()) && getFirstToken().isImplicit();
    }

    @Override
    Chars getText();


    /**
     * Root node of Javadoc ASTs.
     */
    class JdocComment extends AbstractJavadocNode implements RootNode {

        private final TextDocument textDocument;

        JdocComment(TextDocument textDocument) {
            super(JavadocNodeId.ROOT);
            this.textDocument = textDocument;
        }

        @Override
        public @NonNull TextDocument getTextDocument() {
            return textDocument;
        }

    }

    /** Some text payload for the comment. */
    class JdocCommentData extends AbstractJavadocNode {

        JdocCommentData(JdocToken tok) {
            this(tok, tok);
        }

        JdocCommentData(JdocToken first, JdocToken last) {
            super(JavadocNodeId.COMMENT_DATA);
            setFirstToken(first);
            setLastToken(last);
        }

        /** Returns the significant text of this comment. */
        public String getData() {
            return IteratorUtil.toStream(GenericToken.range(getFirstToken(), getLastToken()))
                               .filter(it -> it.getKind() == JdocTokenType.COMMENT_DATA)
                               .map(JdocToken::getImage)
                               .collect(Collectors.joining(" "));
        }

    }

    /**
     * An HTML character reference. This comes in two forms:
     * <ul>
     * <li><i>Character entity references</i>, eg {@code &amp;}
     * <li><i>Numeric character references</i>, either decimal
     *     (eg {@code &#0010;}) or hexadecimal (eg {@code &#x00a0;})
     * </ul>
     */
    class JdocCharacterReference extends AbstractJavadocNode {

        private static final Pattern NAMED_ENTITY = Pattern.compile("&(\\w+);");
        private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#([xX])?([0-9a-zA-Z]+);");

        private final String name;
        private final int point;
        private final int base;

        JdocCharacterReference(JdocToken tok) {
            super(JavadocNodeId.CHARACTER_REFERENCE);
            assert tok.getKind() == JdocTokenType.CHARACTER_REFERENCE;
            setFirstToken(tok);
            setLastToken(tok);

            Matcher named = NAMED_ENTITY.matcher(tok.getImage());
            Matcher numeric = NUMERIC_ENTITY.matcher(tok.getImage());

            if (named.matches()) {
                this.name = named.group(1);
                this.point = 0;
                this.base = 0;
            } else if (numeric.matches()) {
                this.name = null;
                this.base = numeric.group(1) != null ? 16 : 10;
                String repr = numeric.group(2);
                this.point = Integer.parseInt(repr, base);
            } else {
                throw new IllegalStateException(tok.getImage() + " is not a valid HTML character reference");
            }
        }

        @Override
        public Chars getText() {
            return getFirstToken().getImageCs();
        }

        /**
         * Returns the constant corresponding to the entity. Returns null
         * if it's unknown.
         */
        public @Nullable KnownHtmlEntity getConstant() {
            return name != null ? KnownHtmlEntity.lookupByName(name)
                                : KnownHtmlEntity.lookupByCode(getCodePoint());
        }

        /**
         * Returns the name of this named entity reference, or null if
         * this is a numeric character reference.
         */
        @Nullable
        public String getName() {
            return name;
        }


        /**
         * Returns the decimal value of the reference, or zero (0) if this
         * is a named character reference.
         * TODO in case of known character reference, should this return the
         *  correct code point?
         */
        public int getCodePoint() {
            return point;
        }

        /**
         * Returns true if this is a hexadecimal numeric character reference.
         */
        public boolean isHexadecimal() {
            return base == 16;
        }

    }

    /** Unexpected token tag. */
    class JdocMalformed extends AbstractJavadocNode {

        private final Set<JdocTokenType> expected;
        private final @Nullable JdocToken actual;

        JdocMalformed(EnumSet<JdocTokenType> expected,
                      @Nullable JdocToken actual) {
            super(JavadocNodeId.MALFORMED);
            this.expected = expected;
            this.actual = actual;
            setFirstToken(actual);
            setLastToken(actual);
        }

        /** Null if EOF. */
        @Nullable
        public JdocToken getActual() {
            return actual;
        }

        public Set<JdocTokenType> getExpected() {
            return expected;
        }

        public String getMessage() {
            if (actual == null) {
                return "Unexpected end of input, expecting " + format(expected);
            }
            String message = "Unexpected " + actual.getKind().format(actual) + " at " + this.actual.getBeginLine() + ":" + this.actual.getBeginColumn();
            if (!expected.isEmpty()) {
                return message + " expecting " + format(expected);
            } else {
                return message;
            }
        }

        @Override
        public String toString() {
            return getMessage();
        }

        private static String format(Set<JdocTokenType> types) {
            if (types.size() == 1) {
                return types.iterator().next().toString();
            }
            return types.stream().map(JdocTokenType::toString).collect(Collectors.joining(", ", "one of ", ""));
        }
    }

    /**
     * Represents an HTML element. This node is pushed for every HTML
     * start tag, and encloses all following nodes until the node is
     * closed. The node may be closed in several ways, see {@link HtmlCloseSyntax}.
     */
    class JdocHtml extends AbstractJavadocNode {

        private final Map<String, JdocHtmlAttr> attributes = new HashMap<>(0);
        private final String tagName;
        private final HtmlTagBehaviour behaviour;
        private HtmlCloseSyntax syntax;

        JdocHtml(String tagName) {
            super(JavadocNodeId.HTML);
            this.tagName = tagName;
            this.behaviour = HtmlTagBehaviour.lookup(tagName);
        }

        JdocHtmlAttr newAttribute(JdocToken first, JdocToken value, JdocToken end, HtmlAttrSyntax syntax) {
            JdocHtmlAttr attr = new JdocHtmlAttr(value, syntax);
            attr.setFirstToken(first);
            attr.setLastToken(end);

            attributes.put(attr.getName(), attr);
            appendChild(attr);
            return attr;
        }

        /** Returns the name of the tag. */
        public String getTagName() {
            return tagName;
        }

        /** Returns the value of an attribute, or null if the attribute has no value. */
        @Nullable
        public JdocHtmlAttr getAttribute(String name) {
            return attributes.get(name);
        }


        @NonNull
        @InternalApi
        HtmlTagBehaviour getBehaviour() {
            return behaviour;
        }

        @InternalApi
        void setCloseSyntax(HtmlCloseSyntax syntax) {
            this.syntax = syntax;
        }

        @NonNull
        public HtmlCloseSyntax getCloseSyntax() {
            assert syntax != null : "Syntax was not set";
            return syntax;
        }

        /**
         * Describe how the HMTL tag was closed (or not).
         */
        enum HtmlCloseSyntax {
            /** Eg {@code <br>}, only for some tags that cannot have any content. */
            VOID,
            /** Eg {@code <a/>}, technically this is only a feature of XHTML, but the parser supports it. */
            XML,
            /**
             * Eg {@code <a></a>}, with a regular HTML end tag. In that case, there
             * will be a {@link JdocHtmlEnd} node as a child of the {@link JdocHtml}.
             */
            HTML,
            /**
             * Eg {@code <li>a<li>b}, the end tag is inferred because some other tag follows,
             * or because the parent is closed. Only valid for some combinations of tags.
             * TODO maybe use an implicit end tag?
             */
            IMPLICIT,
            /** Unclosed, because no {@link #IMPLICIT} condition matched. This is an error. */
            UNCLOSED
        }
    }

    /**
     * An attribute of an {@linkplain JdocHtml HTML node}.
     */
    class JdocHtmlAttr extends AbstractJavadocNode {

        private final @Nullable JdocToken valueToken;
        private final HtmlAttrSyntax syntax;

        JdocHtmlAttr(@Nullable JdocToken valueToken, HtmlAttrSyntax syntax) {
            super(JavadocNodeId.HTML_ATTR);
            this.valueToken = valueToken;
            this.syntax = syntax;
        }


        /**
         * Returns the string value of the attribute. For {@linkplain HtmlAttrSyntax#EMPTY empty attribute syntax},
         * the value is the name of the attribute.
         */
        @NonNull
        public String getValue() {
            return getSyntax() == HtmlAttrSyntax.EMPTY
                   ? getName()
                   : getValueToken() == null
                     ? ""
                     : getValueToken().getImage();
        }

        /** Returns the name of the attribute. */
        @NonNull
        public String getName() {
            return getIdentifierToken().getImage();
        }

        /** Returns the identifier token. */
        @NonNull
        public JdocToken getIdentifierToken() {
            return getFirstToken();
        }

        /**
         * Returns the value token, or null if this is uses the
         * {@linkplain HtmlAttrSyntax#EMPTY empty attribute syntax},
         * or if the value is the empty string.
         */
        @Nullable
        public JdocToken getValueToken() {
            return valueToken;
        }

        /** Returns the syntax used by this attribute. */
        @NonNull
        public HtmlAttrSyntax getSyntax() {
            return syntax;
        }

        /**
         * Kind of syntax for the attribute.
         */
        public enum HtmlAttrSyntax {
            /**
             * Empty-attribute syntax for boolean attribute, eg {@code <option selected>},
             * equivalent to {@code <option selected="selected">}
             */
            EMPTY,
            /** Unquoted attribute, eg {@code <input value=yes>}. */
            UNQUOTED,
            /** Single-quoted attribute, eg {@code <input type='checkbox'>}. */
            SINGLE_QUOTED,
            /** Double-quoted attribute, eg {@code <input name="be evil">}. */
            DOUBLE_QUOTED
        }
    }

    /**
     * The end tag of an HTML element, eg {@code </pre>}.
     */
    class JdocHtmlEnd extends AbstractJavadocNode {

        private final String tagName;

        JdocHtmlEnd(String tagName) {
            super(JavadocNodeId.HTML_END);
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }
    }

    /** An HTML comment. */
    class JdocHtmlComment extends AbstractJavadocNode {

        JdocHtmlComment() {
            super(JavadocNodeId.HTML_COMMENT);
        }

        /** Returns the significant text of this comment. */
        public String getData() {
            return IteratorUtil.toStream(GenericToken.range(getFirstToken(), getLastToken()))
                               .filter(it -> it.getKind() == JdocTokenType.HTML_COMMENT_CONTENT)
                               .map(JdocToken::getImage)
                               .collect(Collectors.joining());
        }
    }

}
