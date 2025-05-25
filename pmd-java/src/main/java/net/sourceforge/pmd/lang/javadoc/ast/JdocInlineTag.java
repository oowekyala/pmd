/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    /**
     *
     */
    private final String tagName;

    JdocInlineTag(String tagName) {
        super(JavadocNodeId.INLINE_TAG);
        this.tagName = tagName;
    }

    /**
     * Returns the tag name. This contains an {@code '@'} character,
     * eg {@code @code}, or {@code @link}.
     */
    public String getTagName() {
        return tagName;
    }


    @Override
    public String getXPathNodeName() {
        // todo memory intensive
        return "Inline" + StringUtils.capitalize(tagName.substring(1));
    }

    public static class JdocUnknownInlineTag extends JdocInlineTag {

        JdocUnknownInlineTag(String tagName) {
            super(tagName);
        }

        @Override
        public String getXPathNodeName() {
            return "UnknownInline";
        }
    }

    /**
     * A {@code {@literal }} or {@code {@code }} tag.
     * Whether this is one or the other can be queried with
     * {@link #isCode()} and {@link #isLiteral()}.
     */
    public static class JdocLiteral extends JdocInlineTag {

        private final String data;

        JdocLiteral(String tagname, String data) {
            super(tagname);
            this.data = data;
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
     * {@link #isLinkPlain()}, {@link #isLink()}.
     */
    public static class JdocLink extends JdocInlineTag {

        private static final Pattern FORMAT = Pattern.compile(
            "((?:[\\w$]+\\.)*[\\w$]+)?"  // type name (g1), null if absent
                + "(?:#"
                + "([\\w$]+)"            // method or field name (g2), null if absent
                + "("                    // params (g3), null if absent
                + "\\([^)]*+\\)"
                + ")?"
                + ")?"
                + "(\\s++(.*))?"            // label (g4), empty if absent
        );
        private final String tname;
        private final String refname;
        private final String args;
        private final String label;

        JdocLink(String tagName, String data) {
            super(tagName);
            if (data != null) {
                Matcher matcher = FORMAT.matcher(data);
                if (matcher.matches()) {
                    tname = matcher.group(1);
                    refname = matcher.group(2);
                    args = matcher.group(3);
                    label = matcher.group(4);
                    return;
                }
            }
            tname = null;
            refname = null;
            args = null;
            label = null;
        }

        /** Returns true if this is a {@code {@linkplain }} tag. */
        public boolean isLinkPlain() {
            return getTagName().endsWith("n");
        }

        /** Returns true if this is a {@code {@link }} tag. */
        public boolean isLink() {
            return getTagName().endsWith("n");
        }

        @Nullable
        public String getTypeName() {
            return tname == null || tname.isEmpty() ? null : tname;
        }

        @Nullable
        public String getFieldName() {
            return refname;
        }

        @Nullable
        public String getArgs() {
            return args;
        }

        @Nullable
        public String getLabel() {
            return label;
        }
    }
}
