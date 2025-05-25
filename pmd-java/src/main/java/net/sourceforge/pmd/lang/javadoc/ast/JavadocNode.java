/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;


/**
 * A node for the Javadoc language. The javadoc AST is pretty simple:
 * <ul>
 *     <li>The root node is always {@link JdocComment}.
 *     <li>Significant text of the comment is represented by {@link JdocCommentData}.
 *     <li>Javadoc inline tags (eg {@code {@code foo}}, are represented by {@link JdocInlineTag}
 *     and its subclasses.
 *     <li>HTML nodes are represented by {@link JdocHtml}.
 *     <li>HTML comments are represented by {@link JdocHtmlComment}.
 *     <li>Malformed HTML is represented by {@link JdocMalformed}.
 *     <li>Whitespace characters, and leading asterisks are only available
 *     in the tokens (see {@link JavadocTokenType#LINE_BREAK} and {@link JavadocTokenType#WHITESPACE}).
 * </ul>
 */
public interface JavadocNode extends TextAvailableNode {


    @Override
    JavadocNode jjtGetParent();
    @Override
    JavadocNode jjtGetChild(int index);


    JavadocToken getFirstToken();
    JavadocToken getLastToken();


    /**
     * Returns the original source code underlying this node. This may
     * contain some insignificant whitespace characters (or asterisks),
     * so use {@link JdocCommentData#getData()} to retrieve the actual content.
     */
    @Override
    String getText();


    /** Root node of Javadoc ASTs. */
    class JdocComment extends AbstractJavadocNode implements RootNode {

        JdocComment() {
            super(JavadocNodeId.ROOT);
        }

    }

    /** Some text payload for the comment */
    class JdocCommentData extends AbstractJavadocNode {

        JdocCommentData(JavadocToken tok) {
            this(tok, tok);
        }

        JdocCommentData(JavadocToken first, JavadocToken last) {
            super(JavadocNodeId.COMMENT_DATA);
            setFirstToken(first);
            setLastToken(last);
        }

        /** Returns the significant text of this comment. */
        public String getData() {
            return getFirstToken().rangeTo(getLastToken())
                                  .filter(it -> it.getKind() == JavadocTokenType.COMMENT_DATA)
                                  .map(JavadocToken::getImage)
                                  .collect(Collectors.joining(" "));
        }

    }

    /** Unexpected token tag. */
    class JdocMalformed extends AbstractJavadocNode {

        private final Set<JavadocTokenType> expected;
        private final @Nullable JavadocToken actual;

        JdocMalformed(Set<JavadocTokenType> expected,
                      @Nullable JavadocToken actual) {
            super(JavadocNodeId.MALFORMED);
            this.expected = expected;
            this.actual = actual;
            setFirstToken(actual);
            setLastToken(actual);
        }

        /** Null if EOF. */
        @Nullable
        public JavadocToken getActual() {
            return actual;
        }

        public Set<JavadocTokenType> getExpected() {
            return expected;
        }

        public String getMessage() {
            if (actual == null) {
                return "Unexpected end of input, expecting " + format(expected);
            }
            String message = "Unexpected token " + actual.getKind() + " at " + this.actual.getBeginLine() + ":" + this.actual.getBeginColumn();
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

        private static String format(Set<JavadocTokenType> types) {
            if (types.size() == 1) {
                return types.iterator().next().toString();
            }
            return types.stream().map(JavadocTokenType::toString).collect(Collectors.joining(", ", "one of ", ""));
        }
    }

    /**
     * Represents an HTML element. HTML elements may be closed in one of three ways:
     * <ul>
     *     <li>With a close tag, eg {@code <p>Text</p>}, in that case, there
     *     will be a {@link JdocHtmlEnd} node as a child of the {@link JdocHtml}.
     *     <li>With an autoclose tag, eg {@code <br/>}. Technically this is
     *     only a feature of XHTML, but the parser supports it. In that case,
     *     {@link JdocHtml#isAutoclose()} returns true.
     *     <li>Implicitly, because eg the parent tag is closed, or some following
     *     opening tag implies that this tag ends (eg {@code <li> A <li> B}).
     * </ul>
     */
    class JdocHtml extends AbstractJavadocNode {

        private final Map<String, JdocHtmlAttr> attributes = new HashMap<>(0);
        private final String tagName;
        private boolean autoclose;
        private final HtmlTagBehaviour behaviour;

        JdocHtml(String tagName) {
            super(JavadocNodeId.HTML);
            this.tagName = tagName;
            this.behaviour = HtmlTagBehaviour.lookup(tagName);
        }

        void addAttribute(JdocHtmlAttr attr) {
            attributes.put(attr.getName(), attr);
            appendChild(attr);
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

        /**
         * Returns true if this element is closed with an XHTML autoclosing tag,
         * eg {@code <br/>}.
         */
        public boolean isAutoclose() {
            return autoclose;
        }


        @NonNull
        @InternalApi
        HtmlTagBehaviour getBehaviour() {
            return behaviour;
        }

        @InternalApi
        void setAutoclose() {
            this.autoclose = true;
        }
    }

    /**
     * An attribute of an {@linkplain JdocHtml HTML node}.
     */
    class JdocHtmlAttr extends AbstractJavadocNode {

        private final @Nullable JavadocToken valueToken;
        private final HtmlAttrSyntax syntax;

        JdocHtmlAttr(@Nullable JavadocToken valueToken, HtmlAttrSyntax syntax) {
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
        public JavadocToken getIdentifierToken() {
            return getFirstToken();
        }

        /**
         * Returns the value token, or null if this is uses the
         * {@linkplain HtmlAttrSyntax#EMPTY empty attribute syntax},
         * or if the value is the empty string.
         */
        @Nullable
        public JavadocToken getValueToken() {
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
            return getFirstToken().rangeTo(getLastToken())
                                  .filter(it -> it.getKind() == JavadocTokenType.HTML_COMMENT_CONTENT)
                                  .map(JavadocToken::getImage)
                                  .collect(Collectors.joining());
        }
    }

}
