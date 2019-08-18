/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.exprs;

import java.util.Objects;

class AndConditionImpl implements EdgeCondition {


    private final EdgeCondition left;
    private final EdgeCondition right;

    AndConditionImpl(EdgeCondition left, EdgeCondition right) {
        this.left = left;
        this.right = right;
    }

    public EdgeCondition getLeft() {
        return left;
    }

    public EdgeCondition getRight() {
        return right;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        maybeParens(sb, left);
        sb.append(" && ");
        maybeParens(sb, right);

        return sb.toString();
    }

    private void maybeParens(StringBuilder sb, EdgeCondition right) {
        if (right instanceof OrConditionImpl) {
            sb.append('(').append(right).append(')');
        } else {
            sb.append(right);
        }
    }

    @Override
    public EdgeCondition negate() {
        // !(a && b) -> !a || !b
        return left.negate().or(right.negate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AndConditionImpl that = (AndConditionImpl) o;
        return Objects.equals(left, that.left)
            && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
