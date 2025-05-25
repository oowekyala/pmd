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
    public static class JdocReturnTag extends JdocBlockTag {

        JdocReturnTag(String tagName) {
            super(JavadocNodeId.RETURN_TAG, tagName);
        }
    }

    /** An {@code @author}. */
    public static class JdocAuthorTag extends JdocBlockTag {

        JdocAuthorTag(String tagName) {
            super(JavadocNodeId.AUTHOR_TAG, tagName);
        }
    }
    /** An {@code @since}. */
    public static class JdocSinceTag extends JdocBlockTag {

        JdocSinceTag(String tagName) {
            super(JavadocNodeId.SINCE_TAG, tagName);
        }
    }

}
