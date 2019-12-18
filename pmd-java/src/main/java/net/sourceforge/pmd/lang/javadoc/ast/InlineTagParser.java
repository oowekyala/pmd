/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/**
 * Inline tag parser.
 */
interface InlineTagParser {

    /** Returns the tag name. */
    String getName();


    /**
     * Parse an inline tag. When the method is called, the parser's
     * {@link MainJdocParser#head()} is set on the {@link JdocTokenType#TAG_NAME}.
     * When it returns, it should be set on the {@link JdocTokenType#INLINE_TAG_END}.
     *
     * <p>This method does not need to set the first/end tokens on
     * the returned node.
     *
     * @param name   Name of the tag to parse
     * @param parser Parser
     *
     * @return A node for the inline tag
     */
    JdocInlineTag parse(String name, MainJdocParser parser);


    /**
     * Block tag parser.
     */
    interface BlockTagParser {


        /** Returns the tag name. */
        String getName();


        /**
         * Parse a block tag. Actually this only parses the argument to the
         * tag, eg the {@code xs} in {@code @param xs}. The remainder of the
         * argument, if it should be parsed normally (ie as a block of
         * comment data, possibly with HTML and inline tags), is left in the
         * token stream.
         *
         * <p>When the method is called, the parser's {@link MainJdocParser#head()}
         * is set on the {@link JdocTokenType#TAG_NAME}. When it returns, it should
         * be ready to parse the rest as comment data.
         *
         * @param name   Name of the tag to parse
         * @param parser Parser
         *
         * @return A node for the inline tag
         */
        JdocBlockTag parse(String name, MainJdocParser parser);
    }
}
