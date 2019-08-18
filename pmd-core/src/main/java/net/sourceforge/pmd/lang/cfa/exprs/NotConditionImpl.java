/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.exprs;

import java.util.Objects;

class NotConditionImpl implements EdgeCondition {

    private final EdgeCondition negated;

    NotConditionImpl(EdgeCondition negated) {
        this.negated = negated;
    }

    @Override
    public String toString() {
        return "!" + negated + "";
    }

    @Override
    public EdgeCondition negate() {
        return negated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotConditionImpl that = (NotConditionImpl) o;
        return negated.equals(that.negated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(negated);
    }
}
