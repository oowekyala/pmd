/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.util.document.Chars;

/**
 * An inline javadoc tag, eg {@code @return}.
 */
public class JdocBlockTag extends AbstractJavadocNode {

    @Nullable String paramName;
    private final String tagName;

    JdocBlockTag(JavadocNodeId id, String tagName) {
        super(id);
        this.tagName = tagName;
    }

    JdocBlockTag(String tagName) {
        this(JavadocNodeId.SIMPLE_BLOCK_TAG, tagName);
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


    void setParamName(@NonNull JdocToken paramToken) {
        assert paramToken.getKind() == JdocTokenType.PARAM_NAME;
        Chars image = paramToken.getImageCs();
        if (image.startsWith('<', 0)) {
            this.paramName = image.substring(1, image.length() - 2);
        } else {
            this.paramName = paramToken.getImage();
        }
    }

    /**
     * If this is an {@code @param} tag and a name was mentioned, returns
     * the identifier for the parameter. For a type parameter, returns the
     * simple name, not {@code <T>}.
     */
    public @Nullable String getParamName() {
        return paramName;
    }

    /** Any known block tag. */
    public static class JdocSimpleBlockTag extends JdocBlockTag {

        JdocSimpleBlockTag(JavadocNodeId id, String tagName) {
            super(id, tagName);
        }

        JdocSimpleBlockTag(String tagName) {
            this(JavadocNodeId.SIMPLE_BLOCK_TAG, tagName);
        }

    }
}
