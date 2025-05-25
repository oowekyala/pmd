/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;


public final class JavadocParserUtils {

    /**
     * Parse the region of the file text enclosed by the start and end
     * as a {@link JdocComment}. The region must start with the token {@code /*}
     * and end with the token {@code * /}.
     *
     * @param fullFileText   File text
     * @param startInclusive Start offset
     * @param endExclusive   End offset
     *
     * @return A comment
     */
    public static JdocComment parseJavadoc(String fullFileText, int startInclusive, int endExclusive) {
        return new JavadocParser(new JavadocLexer(fullFileText, startInclusive, endExclusive)).parse();
    }
}
