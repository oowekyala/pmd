/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.util.EnumSet;

/**
 * Token types for {@link JdocToken}.
 */
public enum JdocTokenType {

    /* ****************
     * Javadoc proper *
     **************** */


    /**
     * Start of input. Any parsable comment must at the very least start
     * with this token.
     */
    COMMENT_START("/**", true),
    /** End of input. */
    COMMENT_END("*/", true) {
        @Override
        public String format(JdocToken token) {
            return "comment delimiter";
        }
    },

    /** Name of an inline or block tag. This includes the preceding @ sign. */
    TAG_NAME("@<tag name>", false) {
        @Override
        public String format(JdocToken token) {
            return "tag name " + token.getImage();
        }
    },

    /**
     * Whitespace ignored by javadoc. Significant whitespace is parsed as {@link #COMMENT_DATA}.
     * Whitespace tokens are significant in {@code <pre>} HTML tags though.
     */
    WHITESPACE("<whitespace>", false),

    /**
     * This also takes care of any following asterisk. Leading whitespace
     * after a line break (or asterisk), and trailing whitespace before
     * the line break, is treated as {@link #WHITESPACE}.
     *
     * <p>Line break tokens are always insignificant for parsing. In a
     * {@code <pre>} HTML tag, it's rendered as a line terminator. Outside,
     * it's rendered as a single space.
     * <pre>
     * lineBreak ::= ("\n" | "\r\n") ({:whitespace:}* "*")?
     * </pre>
     */
    LINE_BREAK("<line break>", false) {
        @Override
        public String format(JdocToken token) {
            return "line break";
        }
    },

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

    /** Significant text for the HTML output. */
    COMMENT_DATA("<comment data>", false),

    /* *****************************
     * References to Java elements *
     ***************************** */


    TYPE_REFERENCE("type reference", false),
    MEMBER_REFERENCE("member reference", false),
    REF_POUND("#", true),
    REF_LPAREN("(", true),
    REF_RPAREN(")", true),
    REF_COMMA(",", true),

    /**
     * Catch all rule for things that don't match anything. Normally
     * should never occur anywhere, bad characters (like invalid usage
     * of {@code <>&}) are lexed as {@link #COMMENT_DATA}.
     */
    BAD_CHAR("<bad character>", false),

    /* *************
     * HTML tokens *
     ************* */

    /** Identifier of an HTML attribute or tag. */
    HTML_IDENT("<identifier>", false),

    /** An HTML character reference. See {@link JavadocNode.JdocCharacterReference}. */
    CHARACTER_REFERENCE("<HTML entity>", false),


    HTML_LT("<", true),
    HTML_GT(">", true),
    HTML_LCLOSE("</", true),
    HTML_RCLOSE("/>", true),

    HTML_EQ("=", true),
    HTML_SQUOTE("'", true),
    HTML_DQUOTE("\"", true),
    HTML_ATTR_VAL("<attribute value>", false),

    HTML_COMMENT_START("<!--", true),
    HTML_COMMENT_END("-->", true),
    HTML_COMMENT_CONTENT("<comment>", false),


    /** An implicit token, generated to support an implicit node. */
    EXPECTED_TOKEN("", true);

    static final EnumSet<JdocTokenType> ATTR_DELIMITERS = EnumSet.of(HTML_SQUOTE, HTML_DQUOTE);
    static final EnumSet<JdocTokenType> EMPTY_SET = EnumSet.noneOf(JdocTokenType.class);

    private final boolean isConst;
    private String value;

    JdocTokenType(String value, boolean isConst) {
        this.value = value;
        this.isConst = isConst;
    }


    public String format(JdocToken token) {
        assert token.getKind() == this;
        return isConst ? "token '" + token.getImage() + "'"
                       : "token '" + token.getImage() + "' (" + this + ")";
    }


    boolean isSignificant() {
        return this != WHITESPACE && this != LINE_BREAK;
    }

    /**
     * If true, {@link #getConstValue()} returns the image of the token.
     * It can be shared between all tokens of this type (no need to allocate
     * a string).
     */
    boolean isConst() {
        return isConst;
    }

    /**
     * See {@link #isConst()}. If that method returns false, then the
     * return value of this method is unspecified.
     */
    String getConstValue() {
        return value;
    }

    @Override
    public String toString() {
        return isConst ? "'" + value + "'" : value;
    }
}
