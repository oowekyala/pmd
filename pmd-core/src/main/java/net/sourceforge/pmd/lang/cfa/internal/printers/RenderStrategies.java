/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.printers;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

/**
 * Strategies to label nodes, etc.
 *
 * @author Clément Fournier
 */
public interface RenderStrategies<N> {


    default String renderAstNode(N astNode) {
        return astNode.toString();
    }

    default String renderBlockLabel(BasicBlock<N> block) {
        return block.toString();
    }

    default String renderEdgeLabel(EdgeCondition cond) {
        return cond.toString();
    }

}
