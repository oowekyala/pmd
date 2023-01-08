/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;

/**
 * An javadoc tag that delimits a block, eg {@code @return} or {@code @param}.
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
     * Tests the name of this tag.
     */
    public boolean isA(String tagName){
        return tagName.equals(getTagName());
    }

    /**
     * Returns the tag name. This contains an {@code '@'} character,
     * eg {@code @return}, or {@code @param}.
     */
    public String getTagName() {
        return tagName;
    }

    @Override
    public FileLocation getReportLocation() {
        return getFirstToken().getReportLocation(); // report on the tag name
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
            // keep the '<' to be able to interpret it later, but remove the '>'
            this.paramName = image.substring(0, image.length() - 1);
        } else {
            this.paramName = paramToken.getImage();
        }
    }

    /**
     * If this is a {@code @param} tag and a name was mentioned, returns
     * the identifier for the parameter. For a type parameter, returns the
     * simple name, not {@code <T>}.
     */
    public @Nullable String getParamName() {
        if (isTypeParamRef()) {
            return paramName.substring(1);
        }
        return paramName;
    }

    /**
     * Returns true if this is a {@code @param} tag for some type parameter.
     */
    public boolean isTypeParamRef() {
        return paramName != null && paramName.startsWith("<");
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
