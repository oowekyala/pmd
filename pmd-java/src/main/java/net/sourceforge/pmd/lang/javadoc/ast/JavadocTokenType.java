/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.util.EnumSet;

public enum JavadocTokenType {
    /** End of input. */
    COMMENT_START("/**", true),
    /** End of input. */
    COMMENT_END("*/", true),

    /**
     * Name of an inline or block tag. This includes the preceding @ sign.
     */
    TAG_NAME("@<tag name>", false),

    /**
     * Whitespace ignored by javadoc. Significant whitespace is parsed as {@link #COMMENT_DATA}.
     * Whitespace tokens are significant in {@code <pre>} HTML tags though.
     */
    WHITESPACE("<whitespace>", false),

    /**
     * This also takes care of any following asterisk. Leading whitespace
     * after a line break (incl asterisk), and trailing whitespace before
     * the line break, is treated as {@link #WHITESPACE}.
     *
     * <p>Line break tokens are always insignificant for parsing. In a
     * {@code <pre>} HTML tag, it's rendered as a line terminator. Outside,
     * it's rendered as a single space.
     * <pre>
     * lineBreak ::= ("\n" | "\r\n") ({:whitespace:}* "*")?
     * </pre>
     */
    LINE_BREAK("<line break>", false),

    /**
     * Always followed by a {@link #TAG_NAME} (otherwise treated as comment data).
     */
    INLINE_TAG_START("{", true),

    /**
     * Produced only when there is a corresponding tag to close. In particular,
     * an inline tag may be ended abruptly by the opening of a block tag, or EOI,
     * in which case the inline tag node may not have an INLINE_TAG_END.
     */
    INLINE_TAG_END("}", true),

    /**
     * Significant text for the HTML output.
     */
    COMMENT_DATA("<comment data>", false),

    BAD_CHAR("<bad character>", false),

    HTML_LT("<", true),
    HTML_GT(">", true),
    HTML_LCLOSE("</", true),
    HTML_RCLOSE("/>", true),

    /*
        Attributes.
     */

    HTML_EQ("=", true),
    HTML_SQUOTE("'", true),
    HTML_DQUOTE("\"", true),
    HTML_ATTR_VAL("<attribute value>", false),

    HTML_COMMENT_START("<!--", true),
    HTML_COMMENT_END("-->", true),
    HTML_COMMENT_CONTENT("<comment>", false),

    HTML_IDENT("<identifier>", false),
    HTML_ENTITY("<HTML entity>", false),
    ;

    static final EnumSet<JavadocTokenType> ATTR_DELIMITERS = EnumSet.of(HTML_SQUOTE, HTML_DQUOTE);

    private final boolean isConst;
    private String value;

    JavadocTokenType(String value, boolean isConst) {
        this.value = value;
        this.isConst = isConst;
    }

    boolean isSignificant() {
        return this != WHITESPACE && this != LINE_BREAK;
    }

    public boolean isConst() {
        return isConst;
    }

    public String getConstValue() {
        return value;
    }

    @Override
    public String toString() {
        return isConst ? "'" + value + "'" : value;
    }
}
