/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An inline javadoc tag, eg {@code {@code }}. */
public abstract class JdocInlineTag extends AbstractJavadocNode {

    private final String tagName;

    JdocInlineTag(String tagName) {
        super(JavadocNodeId.INLINE_TAG);
        this.tagName = tagName;
    }

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
     * {@code {@link }} or {@code {@linkplain }} tag.
     */
    public static class JdocLink extends JdocInlineTag {

        private static final Pattern FORMAT = Pattern.compile(
            "(?: ((?:[\\w$]+\\.)*(?:[\\w$]+)?))" // type name (g1), empty if absent
                + "(?:#"
                + "([\\w$]+)"                    // method or field name (g2), null if absent
                + "("                            // params (g3), null if absent
                + "\\([^)]*+\\)"
                + ")?"
                + ")?"
                + "\\s++(.*)"                    // label (g4), empty if absent
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

        public String getLabel() {
            return label;
        }
    }
}
