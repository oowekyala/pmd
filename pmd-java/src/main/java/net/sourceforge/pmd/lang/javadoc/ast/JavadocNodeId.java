/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/** Ids for javadoc nodes. */
enum JavadocNodeId {
    ROOT("Comment"),
    COMMENT_DATA("Data"),
    CHARACTER_REFERENCE("HtmlEntity"),
    WHITESPACE("Whitespace"),

    INLINE_TAG("InlineTag"),
    LITERAL_TAG("InlineLiteralTag"),
    LINK_TAG("InlineLinkTag"),
    UNKNOWN_INLINE_TAG("UnknownInlineTag"),

    BLOCK_TAG("BlockTag"),

    MALFORMED("Malformed"),
    HTML("Html"),
    HTML_ATTR("HtmlAttr"),
    HTML_END("HtmlEnd"),
    HTML_COMMENT("HtmlComment"),
    ;

    private final String xpathName;

    JavadocNodeId(String xpathName) {
        this.xpathName = xpathName;
    }

    public String getXPathNodeName() {
        return xpathName;
    }

}
