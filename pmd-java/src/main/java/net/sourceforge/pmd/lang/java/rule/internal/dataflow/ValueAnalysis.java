/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTCastExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTForeachStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.rule.internal.StablePathMatcher;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.util.DataMap;

/**
 * Base class for flow-sensitive value analysis. An implementation
 * implements an analysis of values using a given {@link ValueModel}.
 * This class contains the hooks that the dataflow framework uses to
 * create and manipulate the models during the dataflow engine's
 * execution.
 *
 * <p>The dataflow framework is structured so that many analyses can
 * be computed at the same time. Analyses can depend on each other.
 * This will allow progressive refinement of our analysis methods
 * through the addition of new models to support more types and
 * data structures.
 *
 * @param <V> Type of value model
 */
public abstract class ValueAnalysis<V extends ValueModel<V>> {

    private final CreateExprModelVisitor createExprModelVisitor;
    final DataMap.SimpleDataKey<V> cacheKey;

    private AnalysisEngine engine;

    protected ValueAnalysis(DataMap.SimpleDataKey<V> cacheKey) {
        this.cacheKey = cacheKey;
        this.createExprModelVisitor = createModelForSimpleExprVisitor();
    }

    private AnalysisEngine getEngine() {
        if (engine == null) {
            throw new IllegalStateException("AnalysisEngine has not been set");
        }
        return engine;
    }

    void setEngine(AnalysisEngine engine) {
        this.engine = engine;
    }

    protected <A extends ValueAnalysis<?>> A getAnalysis(Class<A> analysisClass) {
        return getEngine().getAnalysis(analysisClass);
    }

    /**
     * The top element of the lattice, representing an unknown
     * set of facts.
     */
    protected abstract @NonNull V unknown();

    /**
     * The bottom element of the lattice, representing an uninitialized
     * variable.
     */
    protected abstract @NonNull V empty();

    /**
     * Create the model used for a formal parameter, where the
     * only assumptions we can make come from the type.
     *
     * @param type The type of a value
     */
    protected @NonNull V createModelBasedOnType(JTypeMirror type) {
        return unknown();
    }

    private @NonNull V createModelForSimpleExpr(@NonNull ASTExpression expr, DataflowScope scope) {
        return expr.acceptVisitor(createExprModelVisitor, scope);
    }

    protected abstract CreateExprModelVisitor createModelForSimpleExprVisitor();

    /**
     * Create the model for the default value of a field. Return unknown
     * if this analysis does not support this kind of value and should not
     * track this field.
     *
     * @param varId a field ID
     */
    protected abstract @NonNull V createFieldDefaultModel(ASTVariableId varId);

    protected V createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr, DataflowScope scope) {
        return createModelBasedOnType(varType);
    }

    private @Nullable V createModelForAssignment(AssignmentEntry value, DataflowScope scope) {
        ASTExpression rhs = value.getRhsAsExpression();
        if (rhs != null) { // todo avoid infinite recursion
            return scope.getModel(rhs, this);
        }
        if (value.isFieldDefaultValue()) {
            return createFieldDefaultModel(value.getVarId());
        } else if (value.isFormalParameterInitialValue()) {
            return createModelBasedOnType(value.getDeclaredType());
        } else if (value.isUnbound()) {
            return unknown();
        } else if (value.isForeachVar()) {
            ASTExpression iterableExpr = value.getVarId().ancestors(ASTForeachStatement.class).firstOrThrow().getIterableExpr();
            return createForeachVarModel(value.getVarId(), value.getDeclaredType(), iterableExpr, scope);
        } else if (value.isBlankDeclaration()) {
            return empty();
        }
        return unknown();
    }

    /**
     * Given a boolean expression, what does its being true or
     * false imply about the values it uses? The boolean expr
     * will never be a ternary, an {@code AND}, {@code OR}, {@code XOR}
     * or conditional {@code AND} or {@code OR} expression.
     *
     * @param boolExpr A boolean expression atom
     * @return assumptions
     */
    protected Assumptions backwardsBoolAnalysis(ASTExpression boolExpr, DataflowScope baseScope) {
        return Assumptions.NO_ASSUMPTIONS;
    }

    protected interface DataflowScope {

        /**
         * Get the model of an expression in this scope. This returns
         * the current working model for the expression. If the expression
         * is a reference to a stable path ({@link StablePathMatcher}), we
         * will fetch facts about the variable. Otherwise, if it is the
         * first time we query this model in this scope, we will create
         * a new model with the {@linkplain #createModelForSimpleExprVisitor() visitor}.
         *
         * @param expr     An expression
         * @param analysis The analysis owning the model
         * @param <V>      Type of value model
         */
        <V extends ValueModel<V>> V getModel(ASTExpression expr, ValueAnalysis<V> analysis);

        /**
         * Set the model of the given expression for the rest of this scope.
         * If the expression is a reference to a stable path ({@link StablePathMatcher}),
         * facts about it will be accumulated and queryable in the rest of
         * this scope.
         *
         * @param expr     An expression
         * @param newModel The new model
         * @param analysis The analysis owning the model
         * @param <V>      Type of value model
         */
        <V extends ValueModel<V>> void setModel(ASTExpression expr, @NonNull V newModel, ValueAnalysis<V> analysis);
    }

    /**
     * Base implementation class for a dataflow scope.
     */
    abstract static class DataflowScopeImpl implements DataflowScope {
        private final Map<ValueAnalysis<?>, AnalysisState<?>> analysisStates = new HashMap<>();

        protected DataflowScopeImpl(Map<ValueAnalysis<?>, AnalysisState<?>> analysisStates) {
            this.analysisStates.putAll(analysisStates);
        }
        protected DataflowScopeImpl() {
        }

        protected Map<ValueAnalysis<?>, AnalysisState<?>> cloneStates(boolean preserveFacts) {
            Map<ValueAnalysis<?>, AnalysisState<?>> states = new HashMap<>(analysisStates);
            states.putAll(this.analysisStates);
            if (!preserveFacts) {
                // clear values but preserve map keys
                states.entrySet().forEach(
                    entry -> entry.setValue(new AnalysisState<>())
                );
            }
            return states;
        }

        protected void register(ValueAnalysis<?> analysis) {
            boolean duplicate = null != analysisStates.putIfAbsent(analysis, new AnalysisState<>());
            if (duplicate) {
                throw new IllegalStateException("Cannot register twice: " + analysis);
            }
        }

        protected boolean shouldCacheVariable() {
            return true;
        }

        @Override
        public <V extends ValueModel<V>> V getModel(ASTExpression expr, ValueAnalysis<V> analysis) {
            AnalysisState<V> state = getAnalysisState(analysis);
            if (expr instanceof ASTNamedReferenceExpr) {
                StablePathMatcher matcher = StablePathMatcher.matching(expr);
                V result;
                if (matcher != null) {
                    result = state.varState.get(matcher);
                    if (result == null) {
                        result = mergeReachingDefinitions((ASTNamedReferenceExpr) expr, analysis);
                        if (shouldCacheVariable()) {
                            state.varState.put(matcher, result);
                        }
                    }
                    setResult(expr, analysis, result);
                    return result;
                }
                // This is not an analysable expression. Maybe its model
                // has been set by an assumption? If so we return that
                // model, however if there is no model for the expression
                // we don't ask the visitor, because it would just call back here.
                return state.exprState.getOrDefault(expr, analysis.unknown());
            }


            // cannot use computeifabsent because of ooncurrent modification
            @Nullable V model = state.exprState.get(expr);
            if (model == null) {
                // todo check recursion
                model = analysis.createModelForSimpleExpr(expr, this);
                state.exprState.put(expr, model);
            }
            return model;
        }

        protected <V extends ValueModel<V>> void setResult(ASTExpression e, ValueAnalysis<V> analysis, V model) {
            e.getUserMap().set(analysis.cacheKey, model);
        }

        private <V extends ValueModel<V>> AnalysisState<V> getAnalysisState(ValueAnalysis<V> analysis) {
            @SuppressWarnings("unchecked")
            AnalysisState<V> state = (AnalysisState<V>) analysisStates.get(analysis);
            if (state == null) {
                throw new IllegalStateException("Analysis not registered " + analysis);
            }
            return state;
        }

        /**
         * Return the current reaching definitions for a reference expr.
         * Return an unknown set if the expr cannot be tracked.
         *
         * @param ref A reference expression
         */
        protected abstract ReachingDefinitionSet currentReachingDefs(ASTNamedReferenceExpr ref);

        /** Merge the models for the current reaching definitions of the variable. */
        private <V extends ValueModel<V>> V mergeReachingDefinitions(ASTNamedReferenceExpr expr, ValueAnalysis<V> analysis) {
            ReachingDefinitionSet reaching = currentReachingDefs(expr);
            if (reaching.isNotFullyKnown()) {
                return analysis.unknown();
            }
            V result = analysis.empty();
            for (AssignmentEntry a : reaching.getReaching()) {
                V model = analysis.createModelForAssignment(a, this);
                result = result.join(model);
            }
            return result;
        }

        @Override
        public <V extends ValueModel<V>> void setModel(ASTExpression expr, @NonNull V newModel, ValueAnalysis<V> analysis) {
            AnalysisState<V> state = getAnalysisState(analysis);
            if (expr instanceof ASTNamedReferenceExpr) {
                StablePathMatcher matcher = StablePathMatcher.matching(expr);
                if (matcher != null) {
                    state.varState.put(matcher, newModel);
                }
            }
            state.exprState.put(expr, newModel);
        }

        @SuppressWarnings("unchecked")
        private <V extends ValueModel<V>> boolean absorbCapture(AnalysisState<V> a, AnalysisState<?> b) {
            return a.absorb((AnalysisState<V>) b);
        }

        protected boolean absorb(DataflowScopeImpl other) {
            boolean changed = false;
            for (Map.Entry<ValueAnalysis<?>, AnalysisState<?>> entry : analysisStates.entrySet()) {
                AnalysisState<?> otherState = other.getAnalysisState(entry.getKey());
                changed |= absorbCapture(entry.getValue(), otherState);
            }
            return changed;
        }

        protected boolean hasEmptyValueAnalysisState() {
            // todo this is never going to match because the exprState
            //  is never cleaned up
            return analysisStates
                .values().stream().allMatch(it -> it.varState.isEmpty() && it.exprState.isEmpty());
        }

        /**
         * Delete the facts we know about the given tracked value.
         * Called when the value has been reassigned.
         */
        public void cleanVariableState(StablePathMatcher matcher) {
            analysisStates.forEach((analysis, state) -> {
                // todo remove all that have the matcher as a prefix
                state.varState.remove(matcher);
            });
        }

        static class AnalysisState<V extends ValueModel<V>> {
            /**
             * Map any expression to its model. This is used principally
             * as a cache. TODO think about limiting size.
             */
            final Map<ASTExpression, V> exprState = new HashMap<>();

            /**
             * Map tracked variables to their current model. This can
             * be refined explicitly by the analysis when evaluating conditions.
             */
            final Map<StablePathMatcher, V> varState = new HashMap<>();

            private static <K, V extends ValueModel<V>> boolean joinMap(Map<K, V> myMap, Map<K, V> otherMap) {
                boolean changed = false;
                for (Map.Entry<K, V> entry : otherMap.entrySet()) {
                    V oldValue = myMap.get(entry.getKey());
                    V newValue = myMap.merge(entry.getKey(), entry.getValue(), V::join);
                    changed |= !Objects.equals(oldValue, newValue);
                }
                return changed;
            }

            boolean absorb(AnalysisState<V> otherState) {
                boolean changed = joinMap(exprState, otherState.exprState);
                changed |= joinMap(varState, otherState.varState);
                return changed;
            }

            @Override
            public String toString() {
                return "AnalysisState{" +
                       "exprState=" + exprState +
                       ", varState=" + varState +
                       '}';
            }
        }
    }

    /**
     * Represents facts that can be applied to a dataflow scope based
     * on the boolean value of a condition.
     */
    protected static final class Assumptions {
        static final Assumptions NO_ASSUMPTIONS =
            new Assumptions(s -> {
            }, s -> {
            });

        private final Consumer<DataflowScope> whenTrue;
        private final Consumer<DataflowScope> whenFalse;

        public Assumptions(Consumer<DataflowScope> whenTrue, Consumer<DataflowScope> whenFalse) {
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
        }

        public Assumptions and(Assumptions other) {
            return whenTrue(
                this.whenTrue.andThen(other.whenTrue)
            );
        }

        public Assumptions or(Assumptions other) {
            return whenFalse(
                this.whenFalse.andThen(other.whenFalse)
            );
        }

        public static Assumptions whenTrue(Consumer<DataflowScope> whenTrue) {
            return new Assumptions(whenTrue, scope -> {
            });
        }

        public static Assumptions whenFalse(Consumer<DataflowScope> whenFalso) {
            return new Assumptions(scope -> {
            }, whenFalso);
        }

        public void assumeFalseIn(DataflowScope scope) {
            whenFalse.accept(scope);
        }

        public void assumeTrueIn(DataflowScope scope) {
            whenTrue.accept(scope);
        }

        public Assumptions merge(Assumptions other) {
            if (this == NO_ASSUMPTIONS) {
                return other;
            } else if (other == NO_ASSUMPTIONS) {
                return this;
            }

            return new Assumptions(
                this.whenTrue.andThen(other.whenTrue),
                this.whenFalse.andThen(other.whenFalse));
        }

        public Assumptions negate() {
            return new Assumptions(whenFalse, whenTrue);
        }
    }

    /**
     * Return a visitor that creates the model for an expression.
     * This method may query models of strict sub-expressions of
     * the parameter.
     *
     * <p> This should return {@link #unknown()} if this analysis does
     * not understand or care about this kind of expression (this usually
     * would mean a type error).
     */
    protected class CreateExprModelVisitor extends JavaVisitorBase<DataflowScope, @NonNull V> {

        protected final V getModel(ASTExpression e, DataflowScope scope) {
            return scope.getModel(e, ValueAnalysis.this);
        }

        protected final <X extends ValueModel<X>> X getModel(ASTExpression e, DataflowScope scope, Class<? extends ValueAnalysis<X>> analysis) {
            return scope.getModel(e, getAnalysis(analysis));
        }


        @Override
        public V visitExpression(ASTExpression node, DataflowScope scope) {
            // By default, we return top, meaning this analysis doesn't
            // understand or care about expressions for which a visit
            // method is not explicitly overridden.
            return unknown();
        }

        @Override
        public V visit(ASTConditionalExpression node, DataflowScope scope) {
            // Note that we do not need to query the boolean analysis here,
            // because the control-flow order visitor will just take one branch
            // or the other if the boolean analysis can determine the condition
            // is constant.
            V thenBool = getModel(node.getThenBranch(), scope);
            V elseBool = getModel(node.getElseBranch(), scope);
            return thenBool.join(elseBool);
        }

        @Override
        public final V visit(ASTVariableAccess node, DataflowScope scope) {
            throw new IllegalStateException("This should be somehow not ever called");
            // return scope.getModelOfReachingDefs(node, ValueAnalysis.this);
        }

        @Override
        public final V visit(ASTFieldAccess node, DataflowScope scope) {
            // todo maybe we will need to override this for the array length model
            throw new IllegalStateException("This should be somehow not ever called");
            // return scope.getModelOfReachingDefs(node, ValueAnalysis.this);
        }

        @Override
        public V visit(ASTArrayAccess node, DataflowScope scope) {
            // todo. actually we need to be able to query another analysis from here,
            //  to get the corresponding array model
            return unknown();
        }

        @Override
        public V visit(ASTAssignmentExpression node, DataflowScope scope) {
            return getModel(node.getRightOperand(), scope);
        }

        /**
         * Note when overriding this: if the expression is an increment
         * or decrement expression, you need to take care of handling
         * the case where the operand is a variable, because it is going
         * to run into an infinite loop.
         */
        @Override
        public V visit(ASTUnaryExpression node, DataflowScope data) {
            return super.visit(node, data);
        }

        @Override
        public V visit(ASTCastExpression node, DataflowScope scope) {
            return getModel(node.getOperand(), scope);
        }

        @Override
        public V visit(ASTSwitchExpression node, DataflowScope scope) {
            return unknown(); // TODO
        }
    }

}
