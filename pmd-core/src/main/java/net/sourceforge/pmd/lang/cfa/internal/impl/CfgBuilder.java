/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

/**
 * Object responsible for building CFGs.
 */
public abstract class CfgBuilder<N, T extends BlockBuildingCtx<N, T>> {

    /**
     * Builds a CFG from a block of code.
     *
     * @param blockNode Block node
     */
    public FlowGraph<N> buildCfg(N blockNode) {
        FlowGraph<N> cfg = buildInitialGraph(blockNode);
        labelNodes(cfg);
        reduce(cfg);
        return cfg;
    }

    /** Returns a new empty context to start building a CFG. */
    protected abstract T newStartCtx();

    /**
     * Build a node for every statement. To keep the transition between
     * subgraphs simple, this graph may have some dummy sequencing nodes.
     * These may be removed by the {@link #reduce(FlowGraph) reduction}
     * step.
     */
    protected FlowGraph<N> buildInitialGraph(N astBlock) {
        T ctx = newStartCtx();
        BasicBlock<N> last = ctx.visitTopDown(astBlock);
        ctx.link(last, ctx.createTarget(ctx.getNormalEnd(), EdgeCondition.TRUE));
        return ctx.toCfg();
    }

    /**
     * Reduce the graph. The default implementation merges basic blocks
     * that are linked only by an unconditional edge. It also prunes dead
     * code.
     *
     * <p>This performs side effects on the graph.
     *
     * @param cfg CFG to process
     */
    protected void reduce(FlowGraph<N> cfg) {
        GraphReductions<N> reductions = GraphReductions.defaultInstance();
        // TODO make this tunable by the user of the framework
        //  eg Keeping dead links is useful to implement just control flow based rules like
        //      UnnecessaryContinue/Break/Return
        //    but not useful for DFA
        // reductions.pruneDeadCode(cfg);
        // removing dead code may uncover some more trivial transitions
        // so we do this in this order
        reductions.mergeTrivialTransitions(cfg);
    }

    /**
     * Assign labels to the nodes of the CFG, useful for debugging later
     * on.
     */
    protected void labelNodes(FlowGraph<N> cfg) {
        // just for debugging
        int i = 0;
        for (BasicBlock<N> block : cfg.getBlocks()) {
            if (block.getKind().isNormal() || block.getKind() == BlockKind.CATCH) {
                block.asMutable().setDebugId(i++);
            }
        }
    }



}
