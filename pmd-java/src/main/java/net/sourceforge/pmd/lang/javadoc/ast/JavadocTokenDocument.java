/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.impl.TokenDocument;
import net.sourceforge.pmd.util.document.TextDocument;

/**
 * Token document for Javadoc.
 */
public final class JavadocTokenDocument extends TokenDocument<JdocToken> {

    JavadocTokenDocument(TextDocument fullText) {
        super(fullText);
    }


    @Override
    public JdocToken getFirstToken() {
        return null; // TODO
    }
}
