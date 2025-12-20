/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTForeachStatement;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

/**
 * Base class for flow-sensitive value analysis.
 *
 * @param <T>
 */
public abstract class DfAnalysis<T extends ValueModel<T>> {

    private final Map<ASTExpression, Optional<T>> exprCache = new HashMap<>();
    private AnalysisEngine engine;


    void setEngine(AnalysisEngine engine) {
        this.engine = engine;
    }

    private AnalysisEngine getEngine() {
        if (engine == null) {
            throw new IllegalStateException("AnalysisEngine has not been set");
        }
        return engine;
    }

    List<Class<? extends DfAnalysis<?>>> dependentAnalyses() {
        return emptyList();
    }

    protected <X extends ValueModel<X>, A extends DfAnalysis<X>> A getAnalysis(Class<A> analysisClass) {
        return getEngine().getAnalysis(analysisClass);
    }

    /**
     * The top element of the lattice, representing an unknown
     * set of facts.
     */
    protected abstract @NonNull T unknown();

    /**
     * The bottom element of the lattice, representing an uninitialized
     * variable.
     */
    protected abstract @NonNull T empty();

    /**
     * Create the model used for a formal parameter, where the
     * only assumptions we can make come from the type.
     *
     * @param type The type of a value
     */
    protected @NonNull T createModelBasedOnType(JTypeMirror type) {
        return unknown();
    }

    /**
     * Create the model for an expression. Used for assignments for instance.
     * Return null if this analysis does not support this kind of value
     * and should not track this value.
     *
     * @param expr an expression
     */
    protected abstract @Nullable T createModel(@NonNull ASTExpression expr);

    /**
     * Create the model for the default value of a field. Return null if
     * this analysis does not support this kind of value and should not
     * track this field.
     *
     * @param varId a field ID
     */
    protected abstract @Nullable T createFieldDefaultModel(ASTVariableId varId);

    /**
     * Return the model for the given expression. Those are cached to avoid
     * recomputing things too many times.
     */
    protected final @Nullable T getModel(@NonNull ASTExpression e) {
        // cannot use computeifabsent because of ooncurrent modification
        @Nullable Optional<T> cachedModel = exprCache.get(e);
        if (cachedModel == null) {
            T nullability = createModel(e);
            cachedModel = Optional.ofNullable(nullability);
            exprCache.put(e, cachedModel);
        }
        return cachedModel.orElse(null);
    }

    protected T createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr) {
        return createModelBasedOnType(varType);
    }


    protected T computeModelOfReachingDefinitions(ASTNamedReferenceExpr node) {
        DataflowPass.ReachingDefinitionSet reaching = getEngine().getDataflow().getReachingDefinitions(node);
        if (reaching.isNotFullyKnown()) {
            return unknown();
        }
        // todo flow sensitivity is really important if we want to
        //  report something else than EMPTY
        T result = empty();
        for (DataflowPass.AssignmentEntry a : reaching.getReaching()) {
            T nullability = createModel(a);
            result = result.join(nullability);
        }
        return result;
    }

    private @Nullable T createModel(DataflowPass.AssignmentEntry value) {
        ASTExpression rhs = value.getRhsAsExpression();
        if (rhs != null) {
            return getModel(rhs);
        }
        if (value.isFieldDefaultValue()) {
            return createFieldDefaultModel(value.getVarId());
        } else if (value.isFormalParameterInitialValue()) {
            return createModelBasedOnType(value.getDeclaredType());
        } else if (value.isUnbound()) {
            return unknown();
        } else if (value.isForeachVar()) {
            ASTExpression iterableExpr = value.getVarId().ancestors(ASTForeachStatement.class).firstOrThrow().getIterableExpr();
            return createForeachVarModel(value.getVarId(), value.getDeclaredType(), iterableExpr);
        } else if (value.isBlankDeclaration()) {
            return empty();
        }
        return unknown();
    }

}
