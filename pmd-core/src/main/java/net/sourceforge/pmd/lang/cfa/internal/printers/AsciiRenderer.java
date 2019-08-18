/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.cfa.internal.printers;

import java.io.PrintStream;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.CfgRenderer;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

public class AsciiRenderer<N> implements CfgRenderer<N> {

    private final RenderStrategies<N> strat;

    public AsciiRenderer(RenderStrategies<N> strat) {
        this.strat = strat;
    }

    @Override
    public void render(FlowGraph<N> cfg, PrintStream out) {

        for (BasicBlock<N> block : cfg.getBlocks()) {
            out.append("+").append(strat.renderBlockLabel(block)).println();

            block.getStatements().forEach(it -> out.println(strat.renderAstNode(it)));

            for (EdgeTarget<N> outEdge : block.getOutEdges()) {
                out.print("\t\t-> " + outEdge.getBlock());
                if (outEdge.getCondition() != EdgeCondition.TRUE) {
                    out.append(" if (").append(strat.renderEdgeLabel(outEdge.getCondition())).append(")").println();
                }
                out.println();
            }
        }
    }

}
