/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import static net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeDirection.IN;
import static net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeDirection.OUT;
import static net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition.TRUE;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

/**
 * Transformations on the graph.
 */
public class GraphReductions<N> {

    private static final GraphReductions INSTANCE = new GraphReductions();

    /**
     * Merge empty blocks which are only linked by an unconditional edge.
     * A stronger implementation would also consider non-empty blocks, and
     * merge the statements into the previous node. This is what a
     * conventional CFG would be.
     */
    public void mergeTrivialTransitions(FlowGraph<N> cfg) {
        reduce(cfg, this::mergeBlocks);
    }

    /**
     * Remove edges whose condition is false, and then remove all nodes
     * that have become unreachable from the START node.
     */
    public void pruneDeadCode(FlowGraph<N> cfg) {
        Set<BasicBlock<N>> reachable = reduce(cfg, this::removeDeadEdges);
        removeDeadComponents(cfg, reachable);
    }

    /**
     * Traverse the [graph] depth-first from the START, applying the
     * given [reduction] to each node.
     *
     * @param graph     CFG
     * @param reduction Function called on each block. This must return the
     *                  edges that will be visited next (a normal implementation returns {@link
     *                  BasicBlock#getOutEdges()}
     *
     * @return The set of visited nodes
     */
    protected Set<BasicBlock<N>> reduce(FlowGraph<N> graph, Function<BasicBlock<N>, Set<EdgeTarget<N>>> reduction) {

        Stack<BasicBlock<N>> todo = new Stack<>();
        Set<BasicBlock<N>> seen = new HashSet<>();
        todo.push(graph.getStart());

        while (!todo.isEmpty()) {
            BasicBlock<N> top = todo.pop();

            if (seen.add(top)) {
                for (EdgeTarget<N> outEdge : reduction.apply(top)) {
                    todo.add(outEdge.getBlock());
                }
            }
        }

        return seen;
    }

    /**
     * Merge nodes whose only link is unconditional. This does not change
     * the reachability relation of the graph. This is executed by {@link #reduce(FlowGraph, Function)}.
     */
    protected Set<EdgeTarget<N>> mergeBlocks(BasicBlock<N> top) {
        if (!top.getKind().isNormal()) {
            return top.getOutEdges();
        }

        Set<EdgeTarget<N>> inEdges = top.getInEdges();
        if (inEdges.size() == 1 && top.getStatements().isEmpty() && top.getOutEdges().size() == 1) {
            EdgeTarget<N> inPath = inEdges.iterator().next();
            return absorb(inPath.getBlock().asMutable(), top, inPath);
        }


        return top.getOutEdges();
    }

    /**
     * Remove dead edges. Removing dead components is done later
     * based on a reachability search. This is executed by {@link #reduce(FlowGraph, Function)}.
     */
    protected Set<EdgeTarget<N>> removeDeadEdges(BasicBlock<N> top) {
        // here we iterate the out edges so that if a node is unreachable,
        // we won't visit it
        for (EdgeTarget<N> outEdge : new LinkedHashSet<>(top.getOutEdges())) {
            if (outEdge.getCondition().equals(TRUE.negate())) {
                removeOutEdge(top.asMutable(), outEdge);
            }
        }

        // return the changed out edges
        return top.getOutEdges();
    }

    protected void removeDeadComponents(FlowGraph<N> graph, Set<BasicBlock<N>> reachable) {
        reachable.forEach(block -> removeDeadInEdges(block, reachable));
        // those may not have been reachable
        removeDeadInEdges(graph.getUncaughtExceptionSink(), reachable);
        removeDeadInEdges(graph.getEnd(), reachable);
    }

    protected void removeDeadInEdges(BasicBlock<N> block, Set<BasicBlock<N>> reachable) {
        block.asMutable().getEdgesMutable(IN).removeIf(edge -> !reachable.contains(edge.getBlock()));
    }

    /** Removes the in-edge and its corresponding out-edge on the source. */
    private void removeOutEdge(MutableBasicBlock<N> block, EdgeTarget<N> target) {
        block.getEdgesMutable(OUT).remove(target);
        target.getBlock().asMutable().getEdgesMutable(IN).remove(block.asMutable().createEdge(target.getCondition()));
    }

    /**
     * Merge [absorbed] into [block]. Absorbed must be a direct successor of [block].
     * The [block] adopts all out edges of [absorbed].
     *
     * FIXME absorption makes the block become self referential!
     *
     * <p>For example, given the following kind of graph:
     * <pre>
     *  block +---(c1)---> absorbed +---(c2)---> o1
     *                              +---(c3)--> o2
     * </pre>
     * we produce the following graph
     * <pre>
     *  block +---(c1 && c2)---> o1
     *        +---(c1 && c3)---> o1
     * </pre>
     * where [block] has all the statements of [absorbed].
     *
     * @param block    Block absorbing the other
     * @param absorbed Block to remove
     * @param inTarget In-edge of [absorbed] from [block], so {@link EdgeTarget#getBlock()} == block
     */
    protected final Set<EdgeTarget<N>> absorb(MutableBasicBlock<N> block, BasicBlock<N> absorbed, EdgeTarget<N> inTarget) {
        EdgeCondition condition = inTarget.getCondition();
        block.getEdgesMutable(OUT).remove(absorbed.asMutable().createEdge(condition));
        absorbed.asMutable().getEdgesMutable(IN).remove(inTarget);

        for (EdgeTarget<N> outEdge : absorbed.getOutEdges()) {
            // adopt the out edges of the absorbed node
            EdgeCondition joinedCond = outEdge.getCondition().and(condition);
            block.getEdgesMutable(OUT).add(outEdge.getBlock().asMutable().createEdge(joinedCond));

            Set<EdgeTarget<N>> inEdges = outEdge.getBlock().asMutable().getEdgesMutable(IN);

            // remove the inEdge corresponding to outEdge on the target of outEdge
            inEdges.remove(absorbed.asMutable().createEdge(outEdge.getCondition()));
            inEdges.add(block.createEdge(joinedCond));
        }

        block.addStatements(absorbed.getStatements());

        return block.getOutEdges();
    }

    @SuppressWarnings("unchecked")
    public static <N> GraphReductions<N> defaultInstance() {
        return INSTANCE;
    }
}
