/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.cfa.internal.impl.MutableBasicBlock;

/**
 * A basic block of a control flow graph. Basic blocks are sequences of
 * statements which execute one after the one, without yielding control.
 *
 * <p>The resolution of basic blocks is the *statement*. We don't drill
 * down into expressions to extract the exact sequence of subexpressions
 * to be executed. We also don't represent all exceptions that can be thrown
 * (eg NPEs on any pointer dereference, or OutOfMemory when allocating an object).
 *
 * TODO assignments in expressions (especially conditions) pose a hard problem
 *  we may have to split all expressions into their own cfgs...
 *
 * <p>Edges of basic blocks are not ordered, and conditions should not
 * be subsets of one another.
 *
 * <p>Basic blocks use reference equality and don't need to implement
 * any kind of structural equals.
 *
 * @param <N> Type of AST nodes of this language
 */
public interface BasicBlock<N> {

    /** Returns the edges going in the given direction. */
    Set<EdgeTarget<N>> getEdges(EdgeDirection direction);


    /**
     * Returns a set of targets to which this node yields control.
     * The {@link EdgeTarget#getBlock()} of those edges is the target
     * of the edge, while this block is the source.
     */
    default Set<EdgeTarget<N>> getOutEdges() {
        return getEdges(EdgeDirection.OUT);
    }


    /**
     * Returns a set of targets for the ingoing edges. The {@link EdgeTarget#getBlock()}
     * of those edges are the *source* of the edge, while this block is
     * the target.
     */
    default Set<EdgeTarget<N>> getInEdges() {
        return getEdges(EdgeDirection.IN);
    }


    /**
     * Returns the set of statements this node executes in. The list does
     * not necessarily contain instances of statements, there
     * can also be some expression nodes (eg the iterable expression
     * for a foreach statement).
     */
    List<? extends N> getStatements();


    /** The role of this block. */
    BlockKind getKind();


    /**
     * Returns the block to which control would jump if execution of this
     * block throws an exception. It can be of type {@link BlockKind#ERROR},
     * if it's uncaught, or {@link BlockKind#CATCH} if it is.
     *
     * <p>This block has an implicit edge to the error handler, taken if
     * the execution of this block throws any exception.
     *
     * <p>The only node which does not have an error handler is the {@link BlockKind#ERROR}
     * node.
     */
    @Nullable
    BasicBlock<N> getErrorHandler();

    /**
     * Returns a mutable view of this node. Basic block objects are mutable,
     * yet that API is reserved for construction. Inspection should only be
     * done through the {@link BasicBlock} interface.
     */
    @InternalApi
    MutableBasicBlock<N> asMutable();


    /** Direction of an edge. */
    enum EdgeDirection {
        IN, OUT
    }

    /**
     * Represents the "kind" of a block, to identify special blocks that
     * are never reduced.
     */
    enum BlockKind {
        /** @see FlowGraph#getStart() */
        START,
        /** @see FlowGraph#getEnd() */
        END,
        /** @see FlowGraph#getUncaughtExceptionSink() */
        ERROR,
        /** @see FlowGraph#getDanglingJumps() */
        DANGLING_JUMPS,
        /** @see FlowGraph#getOtherErrorHandlers() */
        CATCH,

        /** Any other kind of block, ie not special. */
        NORMAL;

        public boolean isNormal() {
            // Maybe later we'll add more precise distinctions within normal blocks
            // Or maybe not, since this would probably only be useful for debugging,
            // and reduction would remove many nodes, making the info obsolete anyway
            return this == NORMAL;
        }
    }

    /**
     * Represents an edge. The source of the edge is left implicit, so
     * an edge is left for interpretation with an {@link EdgeDirection}.
     */
    interface EdgeTarget<N> {

        /**
         * Returns the condition under which this edge is taken. If this
         * is {@link EdgeCondition#TRUE}, then the edge is always taken.
         */
        EdgeCondition getCondition();


        /**
         * This should be interpreted as the source or target of the
         * edge, depending on whether this comes from {@link BasicBlock#getOutEdges()}
         * or {@link BasicBlock#getInEdges()}.
         */
        BasicBlock<N> getBlock();


    }


}
