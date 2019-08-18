/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.DANGLING_JUMPS;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.END;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.ERROR;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.NORMAL;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.START;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeDirection;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

/**
 * Context object for building a CFG. This keeps track of a {@link #before}
 * block, from which control is coming, and the special nodes of the context.
 * Abrupt completion should be modelled by linking to the {@link #getExceptionSink()}.
 *
 * <p>The default basic block implementation supports parallel edges provided
 * their condition is unequal.
 *
 * @param <N> Type of AST nodes of this language
 * @param <T> Type of the concrete implementation class (F-bound)
 */
public abstract class BlockBuildingCtx<N, T extends BlockBuildingCtx<N, T>> {

    public final MutableBasicBlock<N> before;

    private final Map<BlockKind, BasicBlock<N>> specialNodes;
    /**
     * Accumulator for roots that should be preserved even though they're not reachable from the START.
     * This is shared with all the subcontexts.
     */
    protected final List<BasicBlock<N>> otherRoots;

    /** Constructor for a sub context. */
    protected BlockBuildingCtx(Map<BlockKind, BasicBlock<N>> specialNodes,
                               List<BasicBlock<N>> otherRoots,
                               BasicBlock<N> before) {
        this.specialNodes = specialNodes;
        this.otherRoots = otherRoots;
        this.before = before.asMutable();
    }

    /** Constructor for a start context. The special nodes are all fresh. */
    protected BlockBuildingCtx() {
        specialNodes = new EnumMap<>(BlockKind.class);
        BasicBlock<N> ex = BasicBlockImpl.uncaughtExceptionNode();
        specialNodes.put(ERROR, ex);
        specialNodes.put(DANGLING_JUMPS, createSpecial(DANGLING_JUMPS, ex));

        specialNodes.put(END, createSpecial(END, ex));

        before = createSpecial(START, ex).asMutable();
        specialNodes.put(START, before);

        otherRoots = new ArrayList<>(0);
    }

    // non-overridable
    private BasicBlock<N> createSpecial(BlockKind kind, BasicBlock<N> exceptionSink) {
        return new BasicBlockImpl<>(kind, exceptionSink, emptyList());
    }

    /**
     * Turn this context to a CFG.
     *
     * @throws IllegalArgumentException If some of the required {@link #getSpecialNodes()} are missing.
     */
    FlowGraph<N> toCfg() {
        return new FlowGraph<>(specialNodes, otherRoots);
    }


    public Map<BlockKind, BasicBlock<N>> getSpecialNodes() {
        return unmodifiableMap(specialNodes);
    }

    /** Target of throw statements. The returned node may have actual kind {@link BlockKind#CATCH}. */
    public BasicBlock<N> getExceptionSink() {
        return Objects.requireNonNull(specialNodes.get(ERROR));
    }

    /** Target of return statements. */
    public BasicBlock<N> getNormalEnd() {
        return specialNodes.get(END);
    }

    /** Target of dangling goto, break or continue (undefined label). */
    public BasicBlock<N> getDangling() {
        return specialNodes.get(DANGLING_JUMPS);
    }

    /** Create a new normal block using {@link #createBlock(BlockKind, List)}. */
    public BasicBlock<N> createBlock(List<? extends N> statements) {
        return createBlock(NORMAL, statements);
    }

    /** Create a new empty normal block using {@link #createBlock(BlockKind, List)}. */
    public BasicBlock<N> createFakeBlock() {
        return createBlock(NORMAL, emptyList());
    }

    /**
     * Create a new block. This decouples the code from block implementation.
     *
     * @param kind       Kind of block
     * @param statements Statements for the block
     */
    public BasicBlock<N> createBlock(BlockKind kind, List<? extends N> statements) {
        return new BasicBlockImpl<>(kind, getExceptionSink(), statements);
    }

    /** Create a new edge target for the given block. */
    public EdgeTarget<N> createTarget(BasicBlock<N> block, EdgeCondition condition) {
        return block.asMutable().createEdge(condition);
    }

    /**
     * Visit the given node to build a new subgraph within this context.
     * This method should recurse in preorder on the relevant children of the node.
     * The method should take care of linking to the {@link #before} block
     * if needed (otherwise the subgraph may be unreachable).
     *
     * @param node Node to visit
     * @return The end block of the subgraph defined by the node
     */
    protected abstract BasicBlock<N> visitTopDown(N node);


    /**
     * Creates a generic sub context. The special nodes may be overridden
     * by the map. This allows eg making a special exception sink within
     * try statements, or scoping the labels of labeled statements.
     */
    public abstract T subCtx(Map<BlockKind, BasicBlock<N>> specialNodes, BasicBlock<N> before);


    /** Creates a generic sub context. */
    public T subCtx(BasicBlock<N> before) {
        return subCtx(this.specialNodes, before);
    }


    /**
     * Link two basic blocks both ways:
     * 'from' gets an out-edge
     * 'to' an in-edge
     */
    public void link(BasicBlock<N> from, EdgeTarget<N> to) {
        from.asMutable().getEdgesMutable(EdgeDirection.OUT).add(to);
        to.getBlock().asMutable().getEdgesMutable(EdgeDirection.IN).add(from.asMutable().createEdge(to.getCondition()));
    }

    /** Make a conditional edge {@code this.before -> block}. */
    public void linkFromBefore(BasicBlock<N> block, EdgeCondition condition) {
        link(before, createTarget(block, condition));
    }


}
