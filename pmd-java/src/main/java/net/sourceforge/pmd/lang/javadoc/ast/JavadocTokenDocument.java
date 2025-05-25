/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.impl.TokenDocument;

/**
 * Token document for Javadoc.
 */
public final class JavadocTokenDocument extends TokenDocument<JdocToken> {

    JavadocTokenDocument(String fullText) {
        super(fullText);
    }


    @Override
    public JdocToken getFirstToken() {
        return null; // TODO
    }
}
