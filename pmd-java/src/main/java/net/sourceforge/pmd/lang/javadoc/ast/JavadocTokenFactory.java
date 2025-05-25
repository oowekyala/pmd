/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.CharStream;
import net.sourceforge.pmd.lang.ast.impl.JavaCharStream;
import net.sourceforge.pmd.lang.ast.impl.JavaccToken;
import net.sourceforge.pmd.lang.java.ast.JavaParserTokenManager;


class JavadocTokenFactory {

    static JavaccToken newToken(int kind, CharStream charStream) {
        JavaCharStream jcs = (JavaCharStream) charStream;

        // Most tokens have an entry in there, it's used to share the
        // image string for keywords & punctuation. Those represent ~40%
        // of token instances
        String image = JavaParserTokenManager.jjstrLiteralImages[kind];

        return new JavaccToken(
            kind,
            image == null ? charStream.GetImage() : image,
            jcs.getStartOffset(),
            jcs.getEndOffset(),
            jcs.getTokenDocument()
        );
    }
}
