/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.javadoc.ast.JdocRef.JdocFieldRef;

/**
 * An inline javadoc tag, eg {@code {@code }}.
 * <p>
 * Inline tags are mapped in the following way:
 * <ul>
 *     <li>{@code @code}, {@code @literal}: {@link JdocLiteral}</li>
 *     <li>{@code @link}, {@code @linkplain}: {@link JdocLink}</li>
 *     <li>{@code @value}: {@link JdocValue}</li>
 *     <li>{@code @inheritDoc}: {@link JdocInheritDoc}</li>
 *     <li>Anything else: {@link JdocUnknownInlineTag}</li>
 * </ul>
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
         * Returns true if this is an {@code {@literal }} tag, false if
         * this is an {@code {@code }} tag.
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
     * A {@code {@link }} or {@code {@linkplain }} tag. Whether this is
     * one or the other can be queried with {@link #isPlain()}. The only
     * difference is that {@code {@link }} is rendered with a monospace
     * font, while {@code {@linkplain }} is not.
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
            return getTagName().charAt(getTagName().length() - 1) == 'n';
        }

        /**
         * Returns the ref, or null if this node has none and is thus
         * invalid wrt to javadoc spec.
         */
        @Nullable
        JdocRef getRef() {
            return children(JdocRef.class).first();
        }

        /**
         * Returns the label text, or null if there is no label.
         */
        public @Nullable CharSequence getLabel() {
            return children(JdocCommentData.class).firstOpt().map(JdocCommentData::getData).orElse(null);
        }
    }

    /**
     * A {@code {@value }} tag displays constant values. When the
     * tag is used without an argument in the documentation
     * comment of a static field, it displays the value of that constant.
     *
     * <p>Semantic errors:
     * <ul>
     * <li>Tag does not refer to a field of this compilation unit
     * <li>Tag has no ref and is not on a constant field
     * </ul>
     */
    public static class JdocValue extends JdocInlineTag {

        JdocValue(String tagName) {
            super(JavadocNodeId.VALUE_TAG, tagName);
        }

        /**
         * Returns the ref, or null if this node has none.
         * This should always be a {@link JdocFieldRef}.
         */
        @Nullable
        JdocRef getRef() {
            return children(JdocRef.class).first();
        }
    }

    /**
     * An {@code {@inheritDoc }} tag.
     */
    public static class JdocInheritDoc extends JdocInlineTag {

        JdocInheritDoc(String tagName) {
            super(JavadocNodeId.INHERIT_DOC_TAG, tagName);
        }

    }
    /**
     * A {@code {@snippet }} tag.
     */
    public static class JdocSnippet extends JdocInlineTag {
        // todo attributes and comments
        JdocSnippet(String literalCode) {
            super(JavadocNodeId.SNIPPET_TAG, "@snippet");
        }
    }
}
