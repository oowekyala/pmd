/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;

/**
 * A control flow graph (CFG) models the control flow of a block of code.
 * This representation models transitions between statements.
 *
 * TODO we can't view a subgraph as a graph itself, since returns may
 *   point to an END outside the subgraph
 *
 * <p>A CFG's nodes are {@link BasicBlock}s. Transitions between statements
 * are modeled by conditional edges between nodes. A CFG has the following
 * properties:
 * <ul>
 * <li>A distinguished {@link #getStart() START} node, from which all other
 * nodes are reachable.</li>
 * <li>A distinguished {@link #getEnd() END} node, which represents normal
 * completion of the block</li>
 * <li>A distinguished {@link #getUncaughtExceptionSink() ERROR} node, which represents
 * exceptional completion of the block.</li>
 * </ul>
 *
 * @param <N> Type of AST nodes of this language
 */
public class FlowGraph<N> {

    private static final Set<BlockKind> REQUIRED = EnumSet.range(BlockKind.START, BlockKind.DANGLING_JUMPS);

    private final Map<BlockKind, BasicBlock<N>> specialNodes;

    private final List<BasicBlock<N>> otherErrorHandlers;

    public FlowGraph(Map<BlockKind, BasicBlock<N>> specialNodes, List<BasicBlock<N>> otherErrorHandlers) {
        this.specialNodes = new EnumMap<>(specialNodes);
        this.otherErrorHandlers = otherErrorHandlers;

        Set<BlockKind> missingKeys = EnumSet.noneOf(BlockKind.class);

        for (BlockKind kind : REQUIRED) {
            if (!specialNodes.containsKey(kind)) {
                missingKeys.add(kind);
            }
        }

        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Flow graph needs the keys " + missingKeys);
        }
    }

    /**
     * Special fake block representing the start of the graph. This may
     * contain statements.
     */
    public BasicBlock<N> getStart() {
        return specialNodes.get(BlockKind.START);
    }

    /**
     * Special fake block representing the end of the graph (normal completion).
     * This doesn't contain any statements.
     */
    public BasicBlock<N> getEnd() {
        return specialNodes.get(BlockKind.END);
    }

    /**
     * Special fake block representing uncaught exceptions within the block.
     * Uncaught throw and assert statements link to here. This cannot contain any statements.
     *
     * <p>We don't represent all exceptions that can be thrown (eg NPEs on any pointer dereference, or OutOfMemory when allocating an object).
     * Only explicitly thrown exceptions link to here.
     */
    public BasicBlock<N> getUncaughtExceptionSink() {
        return specialNodes.get(BlockKind.ERROR);
    }

    /**
     * Special fake block representing the target of dangling jumps.
     * This doesn't contain any statements. It may have in-edges when
     * some jump label in a goto, break, or continue references an
     * undefined label.
     */
    public BasicBlock<N> getDanglingJumps() {
        return specialNodes.get(BlockKind.DANGLING_JUMPS);
    }


    /**
     * Returns the roots of other exception handlers (catch statements) in the
     * block. Those may be unreachable from the START node if no exceptions are
     * explicitly thrown within the block of the try. {@link BasicBlock#getErrorHandler()}
     * maps basic blocks to the error handler they're linking to (the innermost enclosing one).
     */
    public List<BasicBlock<N>> getOtherErrorHandlers() {
        return otherErrorHandlers;
    }


    public Stream<BasicBlock<N>> streamBlocks() {

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(allBlocksIterator(), BlockIterator.SPLITER_CHARACTERISTICS),
            false
        );
    }

    /**
     * Returns all the blocks of this graph (including all special nodes,
     * even if they're unreachable). Nodes are yielded in depth-first
     * order.
     */
    public Set<BasicBlock<N>> getBlocks() {
        BlockIterator<N> iter = allBlocksIterator();
        while (iter.hasNext()) {
            iter.next();
        }
        return iter.seen;
    }


    @NonNull
    private BlockIterator<N> allBlocksIterator() {
        BlockIterator<N> iter = new BlockIterator<>();
        iter.addRoot(getStart());
        iter.addRoot(getOtherErrorHandlers());
        iter.addRoot(getEnd());
        iter.addRoot(getUncaughtExceptionSink());
        iter.addRoot(getDanglingJumps());
        return iter;
    }


    private static class BlockIterator<N> implements Iterator<BasicBlock<N>> {


        private static final int SPLITER_CHARACTERISTICS = Spliterator.DISTINCT
            & Spliterator.IMMUTABLE
            & Spliterator.ORDERED
            & Spliterator.NONNULL;

        private final Deque<BasicBlock<N>> stack = new ArrayDeque<>();
        // avoid cycle
        private final Set<BasicBlock<N>> seen = new LinkedHashSet<>();

        void addRoot(BasicBlock<N> b) {
            stack.push(b);
            seen.add(b);
        }

        void addRoot(Collection<? extends BasicBlock<N>> b) {
            stack.addAll(b);
            seen.addAll(b);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public BasicBlock<N> next() {
            BasicBlock<N> top = stack.pop();
            for (EdgeTarget<N> outEdge : top.getOutEdges()) {
                if (seen.add(outEdge.getBlock())) {
                    stack.push(outEdge.getBlock());
                }
            }

            return top;
        }
    }
}
