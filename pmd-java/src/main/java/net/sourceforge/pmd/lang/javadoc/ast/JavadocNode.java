/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.EnumSet;
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
            jjtSetFirstToken(first);
            jjtSetLastToken(last);
        }

        /** Returns the significant text of this comment. */
        public String getData() {
            return jjtGetFirstToken().rangeTo(jjtGetLastToken())
                                     .filter(it -> it.getKind() == JavadocTokenType.COMMENT_DATA)
                                     .map(JavadocToken::getImage)
                                     .collect(Collectors.joining());
        }

    }

    /** Unexpected token tag. */
    class JdocMalformed extends AbstractJavadocNode {

        private final Set<JavadocTokenType> expected;
        private final JavadocToken actual;

        JdocMalformed(EnumSet<JavadocTokenType> expected, JavadocToken token) {
            super(JavadocNodeId.MALFORMED);
            this.expected = expected;
            this.actual = token;
        }

        /** Null if EOF. */
        @Nullable
        public JavadocToken getActual() {
            return actual;
        }

        public Set<JavadocTokenType> getExpected() {
            return expected;
        }
    }

    /**
     * Represents an HTML element.HTML elements may be closed in one of three ways:
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

        public static final String UNATTRIBUTED = null;
        final Map<String, String> attributes = new HashMap<>(0);
        private final String tagName;
        private boolean autoclose;
        private final HtmlTagBehaviour behaviour;

        JdocHtml(String tagName) {
            super(JavadocNodeId.HTML_START);
            this.tagName = tagName;
            this.behaviour = HtmlTagBehaviour.lookup(tagName);
        }

        /** Returns the name of the tag. */
        public String getTagName() {
            return tagName;
        }

        /** Returns the value of an attribute, or null if the attribute has no value. */
        @Nullable
        public String getAttribute(String name) {
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
            return jjtGetFirstToken().rangeTo(jjtGetLastToken())
                                     .filter(it -> it.getKind() == JavadocTokenType.HTML_COMMENT_CONTENT)
                                     .map(JavadocToken::getImage)
                                     .collect(Collectors.joining());
        }
    }

}
