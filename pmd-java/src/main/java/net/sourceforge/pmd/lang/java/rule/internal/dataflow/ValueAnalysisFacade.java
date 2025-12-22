/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.DataflowScope;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExecutableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ReturnScopeNode;
import net.sourceforge.pmd.lang.java.rule.internal.StablePathMatcher;
import net.sourceforge.pmd.util.DataMap;

public class ValueAnalysisFacade {

    private static final DataMap.SimpleDataKey<Nullability> NULLABILITY_RESULT
        = DataMap.simpleDataKey("pmd.dataflow.val.nullability");
    private static final DataMap.SimpleDataKey<BooleanModel> BOOLEAN_MODEL
        = DataMap.simpleDataKey("pmd.dataflow.val.boolean");
    private static final DataMap.SimpleDataKey<ReachingDefinitionSet> SPECULATIVE_REACHING_DEFS
        = DataMap.simpleDataKey("java.dataflow.reaching.speculative");
    private static final DataMap.SimpleDataKey<DetachedScope> DF_SCOPE
        = DataMap.simpleDataKey("java.dataflow.scope_facts");

    public static ValueAnalysisResult process(ASTCompilationUnit acu) {
        AnalysisEngine engine = new AnalysisEngine();
        engine.register(new NullabilityAnalysis());
        engine.register(new BooleanValueAnalysis());

        for (ASTTypeDeclaration typeDecl : acu.getTypeDeclarations()) {
            ValueAnalysisState subResult = new ValueAnalysisState(engine);
            BaseDataflowPass.processTypeDecl(typeDecl, subResult);

        }

        return new ValueAnalysisResult(engine);
    }

    /**
     * A detached analysis scope stores the facts accumulated by a SpanInfo
     * and can be used to query or compute the results of analyses. The current
     * reaching defs are the <i>speculative</i> reaching defs. Those have been
     * obtained by pruning parts of the code through condition speculation.
     * For now, we store it separately from the other reaching defs, because
     * UnusedAssignment relies on the JLS's definite assignment definition.
     * todo reevaluate this. If we can speculate on conditions without FPs,
     *  why wouldn't we want UnusedAssignment to skip dead code?
     */
    private static class DetachedScope extends ValueAnalysis.DataflowScopeImpl {

        DetachedScope(ValueAnalysis.DataflowScopeImpl spanInfo) {
            super(spanInfo.cloneStates(true));
        }

        @Override
        protected boolean shouldCacheVariable() {
            // In detached mode we don't cache the state of variables
            // because we want them to be recomputed based on the
            // reaching definitions.
            //
            // todo we could also not do that and make the cache keys
            //  contain info about the reaching defs.
            return false;
        }

        @Override
        protected ReachingDefinitionSet currentReachingDefs(ASTAssignableExpr.ASTNamedReferenceExpr ref) {
            // todo there is a fallback for final fields
            return ref.getUserMap().computeIfAbsent(SPECULATIVE_REACHING_DEFS,
                () -> ReachingDefinitionsAnalysis.DataflowResult.reachingFallback(ref));
        }
    }

    private static class ValueAnalysisState extends BaseDataflowPass.GlobalAlgoState {

        private final AnalysisEngine engine;
        private final BooleanValueAnalysis boolAnalysis;

        public ValueAnalysisState(AnalysisEngine engine) {
            this.engine = engine;
            boolAnalysis = engine.getAnalysis(BooleanValueAnalysis.class);
        }

        @Override
        protected void exitControlFlowScope(ASTExecutableDeclaration node, ValueAnalysis.DataflowScopeImpl endState) {
            node.getUserMap().set(DF_SCOPE, new DetachedScope(endState));
        }

        @Override
        protected DataMap.SimpleDataKey<ReachingDefinitionSet> reachingDefsKey() {
            return SPECULATIVE_REACHING_DEFS;
        }

        @Override
        protected void newAssignment(BaseDataflowPass.@Nullable VarLocalInfo previous, AssignmentEntry newEntry, ValueAnalysis.DataflowScopeImpl scope) {
            StablePathMatcher matcher = StablePathMatcher.matching(newEntry.var);
            scope.cleanVariableState(matcher);
        }

        @Override
        public BooleanModel speculateCondition(@NonNull ASTExpression expr, DataflowScope scope) {
            return scope.getModel(expr, boolAnalysis);
        }

        @Override
        public void setConditionSpec(ASTExpression expr, BooleanModel spec, DataflowScope scope) {
            scope.setModel(expr, spec, boolAnalysis);
        }

        @Override
        protected Collection<? extends ValueAnalysis<?>> getAnalyses() {
            return engine.getAllAnalyses();
        }
    }

    public static class ValueAnalysisResult {
        private final AnalysisEngine engine;

        private ValueAnalysisResult(AnalysisEngine engine) {
            this.engine = engine;
        }

        private <V extends ValueModel<V>> V getModel(ASTExpression expr, DataMap.SimpleDataKey<V> cacheKey, ValueAnalysis<V> analysis) {
            return expr.getUserMap().computeIfAbsent(cacheKey, () -> {
                ReturnScopeNode scopeBearingNode = expr.ancestors(ReturnScopeNode.class).first();
                if (scopeBearingNode == null) {
                    return analysis.unknown();
                }
                DetachedScope dfScope = scopeBearingNode.getUserMap().get(DF_SCOPE);
                if (dfScope == null) {
                    return analysis.unknown();
                }
                return dfScope.getModel(expr, analysis);
            });
        }
        /**
         * Return the nullability of an expression.
         */
        public Nullability getNullability(ASTExpression expr) {
            return getModel(expr, NULLABILITY_RESULT, engine.getAnalysis(NullabilityAnalysis.class));
        }

        public BooleanModel getBooleanModel(ASTExpression expr) {
            return getModel(expr, BOOLEAN_MODEL, engine.getAnalysis(BooleanValueAnalysis.class));
        }
    }
}
