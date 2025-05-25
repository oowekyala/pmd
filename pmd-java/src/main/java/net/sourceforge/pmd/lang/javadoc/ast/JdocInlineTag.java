/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.metrics.JavaMetrics;

/**
 * An inline javadoc tag, eg {@code {@code }}.
 *
 * Inline tags are mapped in the following way:
 * <ul>
 *     <li>{@code @code}, {@code @literal}: {@link JdocLiteral}</li>
 *     <li>{@code @link}, {@code @linkplain}: {@link JdocLink}</li>
 *     <li>Anything else: {@link JdocUnknownInlineTag}</li>
 * </ul>
 *
 * TODO support all standard doclet tags
 */
public abstract class JdocInlineTag extends AbstractJavadocNode {

    private final String tagName;

    JdocInlineTag(JavadocNodeId id, String tagName) {
        super(id);
        this.tagName = tagName;
    }

    /**
     * Returns the tag name. This contains an {@code '@'} character,
     * eg {@code @code}, or {@code @link}.
     */
    public String getTagName() {
        return tagName;
    }

    /** An unknown inline tag. */
    public static class JdocUnknownInlineTag extends JdocInlineTag {

        private final String data;

        JdocUnknownInlineTag(String tagName, String data) {
            super(JavadocNodeId.UNKNOWN_INLINE_TAG, tagName);
            this.data = data;
        }

        /** Returns the contents of the tag. */
        public String getData() {
            return data;
        }
    }

    /**
     * A {@code {@literal }} or {@code {@code }} tag.
     * Whether this is one or the other can be queried with
     * {@link #isCode()} and {@link #isLiteral()}. The only
     * difference is that {@code {@code }} is rendered
     * with a monospace font, while {@code {@literal }} is
     * not.
     */
    public static class JdocLiteral extends JdocInlineTag {

        private final String data;

        JdocLiteral(String tagName, String data) {
            super(JavadocNodeId.LITERAL_TAG, tagName);
            this.data = data;


            assert tagName.equals("@code")
                || tagName.equals("@literal");
        }

        /**
         * Returns true if this is an {@code {@code }} tag, false if this
         * is an {@code {@literal }} tag.
         */
        public boolean isCode() {
            return "@code".equals(getTagName());
        }

        /**
         * Returns true if this is an {@code {@literal }} tag, false if this
         * is an {@code {@code }} tag.
         */
        public boolean isLiteral() {
            return !isCode();
        }

        /** Returns the tag's contents. */
        public String getData() {
            return data;
        }
    }

    /**
     * A {@code {@link }} or {@code {@linkplain }} tag.
     * Whether this is one or the other can be queried with
     * {@link #isPlain()}. The only
     * difference is that {@code {@link }} is rendered
     * with a monospace font, while {@code {@linkplain }} is
     * not.
     *
     * @see JavaMetrics#get(net.sourceforge.pmd.lang.metrics.MetricKey, ASTAnyTypeDeclaration)
     */
    public static class JdocLink extends JdocInlineTag {

        JdocLink(String tagName) {
            super(JavadocNodeId.LINK_TAG, tagName);

            assert tagName.equals("@link")
                || tagName.equals("@linkplain");

        }

        /**
         * Returns true if this is a {@code {@linkplain }} tag.
         * Otherwise this is a {@code {@link }} tag.
         */
        public boolean isPlain() {
            return getTagName().endsWith("n");
        }

        /**
         * Returns the ref, or null if this node has none and is thus
         * invalid wrt to javadoc spec.
         */
        @Nullable
        JdocRef getRef() {
            return children(JdocRef.class).first();
        }

        @Nullable
        public String getLabel() {
            return children(JdocCommentData.class).firstOpt().map(JdocCommentData::getData).orElse(null);
        }
    }
}
