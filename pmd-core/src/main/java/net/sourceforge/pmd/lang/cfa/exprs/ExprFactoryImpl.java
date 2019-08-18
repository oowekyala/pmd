/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.exprs;

import java.util.Objects;

import net.sourceforge.pmd.lang.ast.TextAvailableNode;

public class ExprFactoryImpl<E> implements SymbolicExprFactory<E> {

    static final ExprFactoryImpl DEFAULT = new ExprFactoryImpl();

    @Override
    public EdgeCondition makeFromAst(E expression) {
        return new AstExpr<>(expression);
    }

    @Override
    public EdgeCondition iterableHasNext(E iterableExpr) {
        return new HasNextCondition<>(iterableExpr);
    }

    @Override
    public EdgeCondition monitorAcquired(E lock) {
        return new MonitorAcquireImpl<>(lock);
    }

    @Override
    public EdgeCondition equality(E left, E right) {
        return new EqualsCondition<>(left, right);
    }

    @Override
    public EdgeCondition catchMatches(E catchParam) {
        return new CatchMatches<>(catchParam);
    }

    @Override
    public EdgeCondition pendingReturn() {
        return SentinelCond.PENDING_RETURN;
    }

    @Override
    public EdgeCondition pendingThrow() {
        return SentinelCond.PENDING_THROW;
    }

    @Override
    public EdgeCondition ifPendingReturn() {
        return SentinelCond.IF_PENDING_RETURN;
    }

    @Override
    public EdgeCondition ifPendingThrow() {
        return SentinelCond.IF_PENDING_THROW;
    }

    private static <E> String toStr(E expr) {
        return expr instanceof TextAvailableNode ? ((TextAvailableNode) expr).getText().toString()
                                                 : expr.toString();
    }

    static class SentinelCond implements EdgeCondition {

        static final SentinelCond PENDING_RETURN = new SentinelCond("@returnPending");
        static final SentinelCond PENDING_THROW = new SentinelCond("@throwPending");
        static final SentinelCond IF_PENDING_THROW = new SentinelCond("if(@throwPending)");
        static final SentinelCond IF_PENDING_RETURN = new SentinelCond("if(@returnPending)");

        private final String toString;

        SentinelCond(String toString) {
            this.toString = toString;
        }

        @Override
        public String toString() {
            return toString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SentinelCond that = (SentinelCond) o;
            return Objects.equals(toString, that.toString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(toString);
        }
    }

    static class CatchMatches<E> implements EdgeCondition {

        private final E catchParam;

        CatchMatches(E catchParam) {
            this.catchParam = catchParam;
        }

        @Override
        public String toString() {
            return "catch(" + toStr(catchParam) + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CatchMatches<?> that = (CatchMatches<?>) o;
            return Objects.equals(catchParam, that.catchParam);
        }

        @Override
        public int hashCode() {
            return Objects.hash(catchParam);
        }
    }

    static class MonitorAcquireImpl<E> implements EdgeCondition {

        private final E lock;

        MonitorAcquireImpl(E lock) {
            this.lock = lock;
        }

        @Override
        public String toString() {
            return "threadHasLock(" + lock + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MonitorAcquireImpl that = (MonitorAcquireImpl) o;
            return Objects.equals(lock, that.lock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lock);
        }

    }

    static class AstExpr<E> implements EdgeCondition {

        private final E expr;

        AstExpr(E expr) {
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "(" + toStr(expr) + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AstExpr that = (AstExpr) o;
            return Objects.equals(expr, that.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }

    static class HasNextCondition<E> implements EdgeCondition {

        final E iterableExpr;

        HasNextCondition(E iterableExpr) {
            this.iterableExpr = iterableExpr;
        }

        @Override
        public String toString() {
            return toStr(iterableExpr) + ".hasNext()";
        }
    }

    static class EqualsCondition<E> implements EdgeCondition {

        private final E left;
        private final E right;


        EqualsCondition(E left, E right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return toStr(left) + " == " + toStr(right);
        }

        @Override
        public EdgeCondition negate() {
            return new NotEqualsCondition<>(this);
        }

        private static class NotEqualsCondition<E> extends NotConditionImpl implements EdgeCondition {

            private EqualsCondition<E> eq;

            NotEqualsCondition(EqualsCondition<E> eq) {
                super(eq);
                this.eq = eq;
            }

            @Override
            public String toString() {
                return toStr(eq.left) + " != " + toStr(eq.right);
            }
        }
    }

}
