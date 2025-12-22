/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

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

    private AnalysisEngine engine;

    protected ValueAnalysis() {
        this.createExprModelVisitor = createModelForSimpleExprVisitor();
    }

    void setEngine(AnalysisEngine engine) {
        this.engine = engine;
    }

    private AnalysisEngine getEngine() {
        if (engine == null) {
            throw new IllegalStateException("AnalysisEngine has not been set");
        }
        return engine;
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


    protected interface DataflowScope {
        <V extends ValueModel<V>> V getModel(ASTExpression expr, ValueAnalysis<V> analysis);

        <V extends ValueModel<V>> V setModel(ASTExpression expr, V newModel, ValueAnalysis<V> analysis);
    }

    private static class DataflowScopeImpl implements DataflowScope {

        private final Map<ValueAnalysis<?>, Map<ASTExpression, Object>> cache = new HashMap<>();

        @Override
        public <V extends ValueModel<V>> V getModel(ASTExpression expr, ValueAnalysis<V> analysis) {
            // cannot use computeifabsent because of ooncurrent modification
            Map<ASTExpression, Object> exprCache = Objects.requireNonNull(cache.get(analysis), "analysis not registered");
            @SuppressWarnings("unchecked")
            @Nullable V model = (V) exprCache.get(expr);
            if (model == null) {
                // todo check recursion
                model = analysis.createModelForSimpleExpr(expr, this);
                exprCache.put(expr, model);
            }
            return model;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V extends ValueModel<V>> V setModel(ASTExpression expr, V newModel, ValueAnalysis<V> analysis) {
            Map<ASTExpression, Object> exprCache = Objects.requireNonNull(cache.get(analysis), "analysis not registered");
            return (V) exprCache.put(expr, newModel);
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
            // understand or care about this kind of expression.
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
        public V visit(ASTVariableAccess node, DataflowScope data) {
            return computeModelOfReachingDefinitions(node);
        }

        @Override
        public V visit(ASTFieldAccess node, DataflowScope data) {
            return computeModelOfReachingDefinitions(node);
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

        @Override
        public V visit(ASTCastExpression node, DataflowScope scope) {
            return getModel(node.getOperand(), scope);
        }

        @Override
        public V visit(ASTSwitchExpression node, DataflowScope scope) {
            return unknown(); // TODO
        }
    }

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

    protected V computeModelOfReachingDefinitions(ASTNamedReferenceExpr node, DataflowScope scope) {
        DataflowPass.ReachingDefinitionSet reaching = getEngine().getDataflow().getReachingDefinitions(node);
        if (reaching.isNotFullyKnown()) {
            return unknown();
        }
        // todo flow sensitivity is really important if we want to
        //  report something else than EMPTY
        V result = empty();
        for (DataflowPass.AssignmentEntry a : reaching.getReaching()) {
            V model = createModelForAssignment(a, scope);
            result = result.join(model);
        }
        return result;
    }

    private @Nullable V createModelForAssignment(DataflowPass.AssignmentEntry value, DataflowScope scope) {
        ASTExpression rhs = value.getRhsAsExpression();
        if (rhs != null) {
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

}
