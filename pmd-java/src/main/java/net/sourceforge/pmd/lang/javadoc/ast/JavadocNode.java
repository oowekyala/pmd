/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.RootNode;


public interface JavadocNode extends Node {


    @Override
    JavadocNode jjtGetChild(int index);


    /** Root node of Javadoc ASTs. */
    class JdComment extends AbstractJavadocNode implements RootNode {

        JdComment() {
            super(JavadocNodeId.ROOT);
        }

    }

    /** Some text payload for the comment */
    class JdCommentData extends AbstractJavadocNode {

        JdCommentData(JavadocToken tok) {
            this(tok, tok);
        }

        JdCommentData(JavadocToken first, JavadocToken last) {
            super(JavadocNodeId.COMMENT_DATA);
            jjtSetFirstToken(first);
            jjtSetLastToken(last);
        }

    }

    /** Whitespace ignored by Javadoc. */
    class JdWhitespace extends AbstractTokenNode {

        JdWhitespace(JavadocToken tok) {
            super(JavadocNodeId.WHITESPACE, tok);
        }

    }

    /** Malformed tag. */
    class JdMalformed extends AbstractJavadocNode {

        private final JavadocTokenType expected;
        private final JavadocToken actual;

        JdMalformed(JavadocTokenType expected, JavadocToken token) {
            super(JavadocNodeId.MALFORMED);
            this.expected = expected;
            this.actual = token;
        }

        /** Null if EOF. */
        @Nullable
        public JavadocToken getActual() {
            return actual;
        }

        public JavadocTokenType getExpected() {
            return expected;
        }

    }

    /** An inline javadoc tag, eg {@code {@code }}. */
    class JdInlineTag extends AbstractJavadocNode {

        private final String tagName;

        JdInlineTag(String tagName) {
            super(JavadocNodeId.INLINE_TAG);
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }
    }

}
