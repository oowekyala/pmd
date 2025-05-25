/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/**
 * An inline javadoc tag, eg {@code @return}.
 */
public abstract class JdocBlockTag extends AbstractJavadocNode {

    private final String tagName;

    JdocBlockTag(JavadocNodeId id, String tagName) {
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

    /** An unknown block tag. */
    public static class JdocUnknownBlockTag extends JdocBlockTag {
        JdocUnknownBlockTag(String tagName) {
            super(JavadocNodeId.UNKNOWN_BLOCK_TAG, tagName);
        }
    }

    /** A {@code @return}. */
    static class JdocSimpleBlockTag extends JdocBlockTag {

        JdocSimpleBlockTag(JavadocNodeId id, String tagName) {
            super(id, tagName);
        }
    }
}
