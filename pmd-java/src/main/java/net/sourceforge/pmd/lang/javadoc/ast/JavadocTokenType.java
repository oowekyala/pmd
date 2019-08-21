/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


public enum JavadocTokenType {
    COMMENT_START("/**", true),
    COMMENT_END("*/", true),

    /** Name of an inline or block tag. */
    TAG_NAME("tag name", false),
    WHITESPACE("whitespace", false),
    INLINE_TAG_START("{", true),
    INLINE_TAG_END("}", true),
    COMMENT_DATA("comment data", false),

    VAL_COMMA(",", true),
    VAL_LPAREN("(", true),
    VAL_RPAREN(")", true),
    VAL_HASH("#", true),
    VAL_PART("identifier", false),

    BAD_CHAR("bad character", false),


    HTML_LT("<", true),
    HTML_GT(">", true),
    HTML_IDENT("identifier", false),
    HTML_ENTITY("HTML entity", false),
    ;

    private final boolean isConst;
    private String value;

    JavadocTokenType(String value, boolean isConst) {
        this.value = value;
        this.isConst = isConst;
    }

    @Override
    public String toString() {
        return isConst ? "\"" + value + "\"" : value;
    }
}
