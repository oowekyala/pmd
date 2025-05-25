/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.util.function.Consumer;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.PrevLinkedToken;
import net.sourceforge.pmd.lang.ast.TokenMgrError;

final class TokenCursor<T extends PrevLinkedToken<T>> {

    private final TokenManager<T> lexer;
    private int offset = 0;
    private @Nullable T tok;
    private boolean isEoi;

    public TokenCursor(TokenManager<T> lexer) {
        this.lexer = lexer;
    }

    /** Returns the current token. */
    public T head() {
        return tok;
    }

    /**
     * Returns true if we advanced for a token. Returns false if end of
     * input is reached. In the latter case, the cursor stays on the last
     * token.
     *
     * @throws TokenMgrError If the lexer throws
     */
    public boolean advance() {
        if (isEoi) {
            return false;
        }
        if (offset > 0) {
            offset--;
            tok = tok.getNext();
            if (tok == null) {
                throw new IllegalStateException();
            }
            return true;
        }
        T t = lexer.getNextToken();
        if (t == null) {
            isEoi = true;
            return false;
        }
        tok = t;
        return true;
    }

    public void reset(T newHead) {
        tok = newHead;
        offset = 0;
        isEoi = newHead == null;
    }

    /**
     * Move the cursor [n] tokens back.
     *
     * @throws IndexOutOfBoundsException If the token manager does not have enough tokens
     */
    public void backup(int n) {
        if (tok == null) {
            throw new IndexOutOfBoundsException();
        }
        while (n-- > 0) {
            tok = tok.getPrevious();
            if (tok == null) {
                throw new IndexOutOfBoundsException();
            }
        }
        offset += n;
    }

    /** Returns true if end of input has been reached. */
    public boolean isEoi() {
        return isEoi;
    }


    /**
     * Consumes token until [stopCondition] is true. All tokens matching
     * the [filter] are fed to the [action]. This method starts by testing
     * the current token.
     */
    public void consumeUntil(Predicate<? super T> stopCondition,
                             Predicate<? super T> filter,
                             Consumer<? super T> action) {

        while (!isEoi && !stopCondition.test(tok)) {
            if (filter.test(tok)) {
                action.accept(tok);
            }
            advance();
        }
    }
}
