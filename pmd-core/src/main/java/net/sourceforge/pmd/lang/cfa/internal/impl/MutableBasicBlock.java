/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

/**
 * @implNote This API is intentionally very stripped-down.
 *     This is to make the construction process clearer.
 */
public interface MutableBasicBlock<N> extends BasicBlock<N> {

    Set<EdgeTarget<N>> getEdgesMutable(EdgeDirection direction);


    void addStatements(List<? extends N> statements);


    EdgeTarget<N> createEdge(EdgeCondition condition);


    void setDebugId(int id);

}
