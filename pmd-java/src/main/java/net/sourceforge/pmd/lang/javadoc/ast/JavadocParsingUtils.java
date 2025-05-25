/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;

/**
 * Main entry point to parse javadoc comments.
 */
public final class JavadocParsingUtils {

    /**
     * Parse the region of the file text enclosed by the start and end
     * as a {@link JdocComment}. The region must start with the token {@code /**}
     * and end with the token {@code * /}.
     *
     * @param fullFileText   File text
     * @param startInclusive Start offset of the comment (index of the opening '/' char)
     * @param endExclusive   End offset of the comment (index after the closing '/' char)
     *
     * @return A Javadoc comment tree
     */
    public static JdocComment parseJavadoc(String fullFileText, int startInclusive, int endExclusive) {
        return new MainJdocParser(new JavadocLexer(fullFileText, startInclusive, endExclusive)).parse();
    }
}
