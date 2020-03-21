/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.TokenMgrError;

/**
 * Common interface for interacting with parser Token Managers.
 */
public interface TokenManager<T extends GenericToken<T>> {

    /**
     * Returns the next token in the chain. When the end of file is reached,
     * the method {@link #isEof(GenericToken)} should return true on that token.
     * If this method returns null, it is assumed the end of file is reached
     * anyway.
     *
     * @throws TokenMgrError If a lex exception occurred.
     */
    @Nullable T getNextToken();


}
