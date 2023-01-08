/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.TextDocument;

/**
 * Token layer of a parsed file.
 */
@Experimental
public abstract class TokenDocument<T extends GenericToken<T>> {
    private final TextDocument textDocument;

    public TokenDocument(TextDocument textDocument) {
        this.textDocument = textDocument;
    }

    /** Returns the original text of the file (without escaping). */
    public Chars getFullText() {
        return textDocument.getText();
    }

    /** Returns the underlying text document. */
    public TextDocument getTextDocument() {
        return textDocument;
    }

    /**
     * Returns the first token of the token chain.
     *
     * @throws IllegalStateException If the document has not been parsed yet
     */
    public abstract T getFirstToken();


    protected static IllegalStateException documentNotReadyException() {
        return new IllegalStateException("Document has not been opened");
    }

}
