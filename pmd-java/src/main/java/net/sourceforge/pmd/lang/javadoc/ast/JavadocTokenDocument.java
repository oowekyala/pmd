/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.impl.TokenDocument;
import net.sourceforge.pmd.lang.document.TextDocument;

/**
 * Token document for Javadoc.
 */
public final class JavadocTokenDocument extends TokenDocument<JdocToken> {

    JdocToken first;

    JavadocTokenDocument(TextDocument fullText) {
        super(fullText);
    }

    void setFirstToken(JdocToken first) {
        this.first = first;
    }

    boolean isOpen() {
        return first != null;
    }

    @Override
    public JdocToken getFirstToken() {
        if (!isOpen()) {
            throw documentNotReadyException();
        }
        return first;
    }
}
