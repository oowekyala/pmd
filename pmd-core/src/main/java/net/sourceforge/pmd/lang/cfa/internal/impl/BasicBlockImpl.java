/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;


class BasicBlockImpl<N> implements BasicBlock<N>, MutableBasicBlock<N>, EdgeTarget<N> {

    private final Set<EdgeTarget<N>> outEdges = new LinkedHashSet<>(2);
    private final Set<EdgeTarget<N>> inEdges = new LinkedHashSet<>(2);
    // Using zero capacity instead of the default constructor prevents
    // the arraylist to grow its array to length 10 on the first insertion.
    // Basic blocks have either 0 or 1 statements.
    private final List<N> statements = new ArrayList<>(0);
    private final BlockKind kind;
    private final BasicBlock<N> errorHandler;
    private int id;


    BasicBlockImpl(BlockKind kind, BasicBlock<N> errorHandler, List<? extends N> statements) {
        this.kind = kind;
        this.errorHandler = errorHandler;
        this.statements.addAll(statements);
    }

    private BasicBlockImpl() {
        this.errorHandler = null;
        this.kind = BlockKind.ERROR;
    }

    static <N> BasicBlock<N> uncaughtExceptionNode() {
        return new BasicBlockImpl<>();
    }

    @Override
    public Set<EdgeTarget<N>> getEdges(EdgeDirection direction) {
        return Collections.unmodifiableSet(getEdgesMutable(direction));
    }

    @Override
    public Set<EdgeTarget<N>> getEdgesMutable(EdgeDirection direction) {
        return direction == EdgeDirection.IN ? inEdges : outEdges;
    }

    @Override
    public Set<EdgeTarget<N>> getOutEdges() {
        return outEdges;
    }

    @Override
    public Set<EdgeTarget<N>> getInEdges() {
        return inEdges;
    }

    @Override
    public List<N> getStatements() {
        return statements;
    }

    @Override
    public BasicBlock<N> getErrorHandler() {
        return errorHandler;
    }

    @Override
    public BlockKind getKind() {
        return kind;
    }

    @Override
    public void addStatements(List<? extends N> statements) {
        this.statements.addAll(statements);
    }

    @Override
    public MutableBasicBlock<N> asMutable() {
        return this;
    }

    @Override
    public EdgeTarget<N> createEdge(EdgeCondition condition) {
        if (condition == EdgeCondition.TRUE) {
            // this is a memory optimisation, to avoid creating a separate
            // EdgeTarget object for this very likely case
            return this;
        }
        return new EdgeTargetImpl<>(this, condition);
    }


    // EdgeTarget methods

    @Override
    public EdgeCondition getCondition() {
        return EdgeCondition.TRUE;
    }

    @Override
    public BasicBlock<N> getBlock() {
        return this;
    }

    @Override
    public void setDebugId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        if (getKind().isNormal()) {
            return "" + id;
        }
        return getKind().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EdgeTarget)) {
            return false;
        }

        EdgeTarget that = (EdgeTarget) o;

        return that.getCondition() == EdgeCondition.TRUE
            && that.getBlock() == this;
    }
}
