/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.document.Document;

/**
 * Maybe this can be used to eg double link tokens, provide an identity
 * for them, idk.
 */
@Experimental
public class TokenDocument {

    private final Document fullText;

    public TokenDocument(CharSequence fullText) {
        this.fullText = Document.forCode(fullText);
    }

    /** Returns the original text of the file (without escaping). */
    public Document getDocument() {
        return fullText;
    }
}
