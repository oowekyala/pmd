/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.cfa.exprs;

import net.sourceforge.pmd.lang.cfa.BasicBlock;

/**
 * Symbolic representation of expressions to condition transitions between
 * {@linkplain BasicBlock basic blocks}.
 *
 * <p>It's pretty unclear when an EdgeCondition really conditions the transition,
 * or when it's just an annotation. Actually this depends on your POV.
 * From a CFA perspective, edge values represent a condition to be fulfilled
 * for the transition to be taken. From a DFA perspective, the edge values
 * are stuff we know is true in the following blocks, and want to use to
 * prove equations about variables.
 *
 * <p>Eg the following graph:
 * <pre>{@code
 *   1 +--(a == b)--> 2
 *     +--(a != b)--> 3
 * }</pre>
 * can have the following interpretations:
 * <ul>
 * <li>CFA: "when in 1, the following block is 2 if eval(a == b), otherwise it's 3",
 * where eval(.) represents a function that computes the condition, whatever its
 * internal structure
 * <li>DFA: "when in 2, we know (a == b), when in 3, we know (a != b)", where these
 * equations don't represent any computation, just a truth about the static values of
 * a and b
 * </ul>
 *
 * <p>The two interpretations coincide in part, but get confusing when
 * you want to represent a condition whose evaluation has side-effects,
 * like {@code (a = b) != null && a.b == x}.
 * Is this really just an edge? or should it be its own subgraph?
 * The CFA perspective doesn't really care, but the DFA one wants assignments
 * to be in basic blocks, and edge annotations to represent truths about the
 * program, not code to be executed, and certainly not something that has its
 * own internal data-flow.
 */
public interface EdgeCondition {


    /**
     * Always true. This is used for unconditional edge. The constant
     * FALSE is written {@link #negate() TRUE.negate()}. FALSE is used
     * for unsatisfiable edges (behind which some dead code may lie).
     */
    EdgeCondition TRUE = new EdgeCondition() {

        final EdgeCondition FALSE = new NotConditionImpl(this) { // SUPPRESS CHECKSTYLE this name is appropriate

            @Override
            public EdgeCondition and(EdgeCondition cond) {
                return FALSE;
            }

            @Override
            public EdgeCondition or(EdgeCondition cond) {
                return cond;
            }

            @Override
            public String toString() {
                return "false";
            }
        };

        @Override
        public String toString() {
            return "true";
        }

        @Override
        public EdgeCondition negate() {
            return FALSE;
        }

        @Override
        public EdgeCondition and(EdgeCondition cond) {
            return cond;
        }

        @Override
        public EdgeCondition or(EdgeCondition cond) {
            return TRUE;
        }
    };


    default EdgeCondition and(EdgeCondition cond) {
        if (cond.equals(TRUE)) {
            return this;
        } else if (cond.equals(TRUE.negate())) {
            return TRUE.negate();
        } else {
            return new AndConditionImpl(this, cond);
        }
    }


    default EdgeCondition or(EdgeCondition cond) {
        if (cond.equals(TRUE)) {
            return TRUE;
        } else if (cond.equals(TRUE.negate())) {
            return this;
        } else {
            return new OrConditionImpl(this, cond);
        }
    }


    /** Returns the negation of this condition. */
    default EdgeCondition negate() {
        // this implementation of boolean logic keeps expressions in
        // negation normal form (to keep them easy to read)
        // Transformations are:
        // !(a && b) -> !a || !b
        // !(a || b) -> !a && !b
        // !!a -> a

        return new NotConditionImpl(this);
    }


    /** Default factory for expressions. */
    @SuppressWarnings("unchecked")
    static <T> SymbolicExprFactory<T> defaultFactory() {
        return ExprFactoryImpl.DEFAULT;
    }


}
