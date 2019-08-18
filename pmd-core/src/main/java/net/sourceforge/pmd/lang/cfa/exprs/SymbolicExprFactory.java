/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.exprs;

/**
 * Builds edge conditions from AST nodes. A language-implementation may
 * override these methods to provide more interesting implementations.
 *
 * @param <N> Type of nodes in this language
 */
public interface SymbolicExprFactory<N> {

    /**
     * A condition based on an AST node. For example, the condition for
     * the "then" branch of an if statement can directly be taken from
     * the AST. The language-specific implementation may break down the
     * AST node to map its structure to the symbolic representation, for
     * later use by a DFA engine.
     */
    EdgeCondition makeFromAst(N expression);


    /** Used to represent an {@code expr.hasNext()} condition, to model foreach statements. */
    EdgeCondition iterableHasNext(N iterableExpr);


    /**
     * Used to represent synchronized statements. The transition is
     * valid if the current thread acquired the monitor of the lock.
     */
    EdgeCondition monitorAcquired(N lock);


    /**
     * Equality between 'left' and 'right'. This is used for switch labels.
     * This should be interpreted as the {@link Object#equals(Object)} relation
     * instead of reference identity. Yet since identity is a sub-relation of
     * equality, identity comparisons can be represented with this object.
     */
    EdgeCondition equality(N left, N right);


    /**
     * Used to model catch statements. The transition is valid if the
     * exception being examined matches the catch parameter (whatever
     * that means for this language).
     */
    EdgeCondition catchMatches(N catchParam);


    /*
        Those are for finally statements. Finally blocks need some context
        because it will yield control to ERROR, END, of 'after' depending
        on why we arrived in the finally. We could duplicate the finally
        subgraph to have these different out-edges, but this would make it
        so that a statement may be in different basic blocks, which is not
        cool for usage of the API. Instead we use these effect annotations.
     */


    /**
     * Effect annotation. This is for return statements in blocks that must yield
     * to a finally. The corresponding test is {@link #ifPendingReturn()}.
     */
    EdgeCondition pendingReturn();


    /**
     * Effect annotation. This is for throw statements in blocks that must yield
     * to a finally. The corresponding test is {@link #ifPendingThrow()}.
     */
    EdgeCondition pendingThrow();


    /**
     * Signifies that the edge is taken if the path being examined from START
     * to the source of the edge has a {@link #pendingReturn()} effect.
     */
    EdgeCondition ifPendingReturn();


    /**
     * Signifies that the edge is taken if the path being examined from START
     * to the source of the edge has a {@link #pendingThrow()} effect.
     */
    EdgeCondition ifPendingThrow();

}
