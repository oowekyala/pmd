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
    LITERAL_TAG("InlineTag"),
    LINK_TAG("InlineTag"),
    VALUE_TAG("InlineTag"),
    INHERIT_DOC_TAG("InlineTag"),
    SNIPPET_TAG("InlineTag"),
    UNKNOWN_INLINE_TAG("UnknownInlineTag"),


    SIMPLE_BLOCK_TAG("BlockTag"),
    UNKNOWN_BLOCK_TAG("UnknownBlockTag"),


    CLASS_REF("ClassRef"),
    EXECUTABLE_REF("ExecutableRef"),
    FIELD_REF("FieldRef"),
    PARAM_REF("ParamRef"),
    TPARAM_REF("TypeParamRef"),

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
