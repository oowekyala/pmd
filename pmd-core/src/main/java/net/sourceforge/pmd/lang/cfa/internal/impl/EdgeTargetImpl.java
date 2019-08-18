/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import java.util.Objects;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;


class EdgeTargetImpl<N> implements EdgeTarget<N> {

    private final BasicBlock<N> end;
    private final EdgeCondition condition;

    EdgeTargetImpl(BasicBlock<N> end, EdgeCondition condition) {
        this.end = end;
        this.condition = condition;
    }

    @Override
    public EdgeCondition getCondition() {
        return condition;
    }

    @Override
    public BasicBlock<N> getBlock() {
        return end;
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
        return end.equals(that.getBlock())
            && getCondition().equals(that.getCondition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, condition);
    }

    @Override
    public String toString() {
        String cond = condition != EdgeCondition.TRUE ? "(" + condition + ")?" : "";


        return cond + "->" + end;
    }
}
