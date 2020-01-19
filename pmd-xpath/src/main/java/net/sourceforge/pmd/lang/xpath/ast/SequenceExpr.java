/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Sequence expression, grouping {@linkplain ASTSequenceExpr SequenceExpr}
 * and {@linkplain ASTEmptySequenceExpr EmptySequenceExpr} under a common type.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface SequenceExpr extends Expr, Iterable<Expr> {


    /**
     * Return true if this is the empty sequence.
     */
    default boolean isEmpty() {
        return getSize() == 0;
    }


    /**
     * Returns the size of this level of the sequence.
     * Nested sequences are actually flattened, such that
     * (a, (b, c)) is a sequence of three elements. This
     * method doesn't care and would return 2.
     *
     * <p>Note that this method cannot return 1.
     */
    int getSize();
}
