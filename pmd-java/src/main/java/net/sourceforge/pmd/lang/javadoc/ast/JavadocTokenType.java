/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


public enum JavadocTokenType {
    COMMENT_START("/**", true),
    COMMENT_END("*/", true),
    EOF("<EOF>", true),

    /** Name of an inline or block tag. */
    TAG_NAME("@<tag name>", false),
    /** Whitespace ignored by javadoc. Significant whitespace is parsed as {@link #COMMENT_DATA}*/
    WHITESPACE("<whitespace>", false),
    /**
     * This also takes care of the following asterisk.
     * <pre>
     * lineBreak ::= ("\n" | "\r\n") {:whitespace:}* "*"?
     * </pre>
     */
    LINE_BREAK("<line break>", false),
    INLINE_TAG_START("{", true),
    INLINE_TAG_END("}", true),
    COMMENT_DATA("<comment data>", false),

    BAD_CHAR("<bad character>", false),

    HTML_LT("<", true),
    HTML_GT(">", true),
    HTML_LCLOSE("</", true),
    HTML_RCLOSE("/>", true),

    HTML_EQ("=", true),
    HTML_ATTR_START("[\"']", false),
    HTML_ATTR_END("[\"']", false),
    HTML_ATTR_VAL("<attribute value>", false),

    HTML_COMMENT_START("<!--", true),
    HTML_COMMENT_END("-->", true),
    HTML_COMMENT_CONTENT("<comment>", false),

    HTML_IDENT("<identifier>", false),
    HTML_ENTITY("<HTML entity>", false),
    ;

    private final boolean isConst;
    private String value;

    JavadocTokenType(String value, boolean isConst) {
        this.value = value;
        this.isConst = isConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public String getConstValue() {
        return value;
    }

    @Override
    public String toString() {
        return isConst ? "\"" + value + "\"" : value;
    }
}
