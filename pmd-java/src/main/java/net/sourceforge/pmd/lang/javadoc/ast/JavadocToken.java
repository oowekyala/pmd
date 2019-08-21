/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.impl.JavaccToken;
import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

public class JavadocToken extends JavaccToken {

    private final JavadocTokenType kind;

    JavadocToken(JavadocTokenType kind, CharSequence image, int startInclusive, int endExclusive, TokenDocument document) {
        super(kind.ordinal(), image, startInclusive, endExclusive, document);
        this.kind = kind;
    }

    public JavadocTokenType getKind() {
        return kind;
    }
}
