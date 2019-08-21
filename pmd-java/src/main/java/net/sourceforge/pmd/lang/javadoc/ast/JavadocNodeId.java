/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/** Ids for javadoc nodes. */
public enum JavadocNodeId {
    ROOT("Comment"),
    COMMENT_DATA("Data"),
    WHITESPACE("Whitespace"),
    INLINE_TAG("InlineTag"),
    BLOCK_TAG("BlockTag"),
    MALFORMED("Error"),
    REF("Ref"),
    ;

    private final String xpathName;

    JavadocNodeId(String xpathName) {
        this.xpathName = xpathName;
    }

    public String getXPathNodeName() {
        return xpathName;
    }

}
