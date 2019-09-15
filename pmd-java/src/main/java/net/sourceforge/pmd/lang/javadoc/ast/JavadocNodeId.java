/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/** Ids for javadoc nodes. */
enum JavadocNodeId {
    ROOT("Comment"),
    COMMENT_DATA("Data"),
    WHITESPACE("Whitespace"),
    INLINE_TAG("InlineTag"),
    BLOCK_TAG("BlockTag"),
    MALFORMED("Error"),
    HTML_START("HtmlStart"),
    HTML_ATTR("HtmlAttr"),
    HTML_END("HtmlEnd"),
    HTML_COMMENT("HtmlComment"),
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
