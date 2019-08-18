/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.exprs;

import java.util.Objects;

class OrConditionImpl implements EdgeCondition {


    private final EdgeCondition left;
    private final EdgeCondition right;

    OrConditionImpl(EdgeCondition left, EdgeCondition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " || " + right;
    }

    @Override
    public EdgeCondition negate() {
        // !(a || b) -> !a && !b
        return left.negate().and(right.negate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrConditionImpl that = (OrConditionImpl) o;
        return Objects.equals(left, that.left)
            && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
