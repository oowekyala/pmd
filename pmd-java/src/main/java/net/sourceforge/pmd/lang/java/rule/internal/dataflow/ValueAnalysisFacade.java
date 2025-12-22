/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.DataflowScope;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.util.DataMap;

public class ValueAnalysisFacade {

    private static final DataMap.SimpleDataKey<Nullability> NULLABILITY_RESULT
        = DataMap.simpleDataKey("pmd.dataflow.val.nullability");
    private static final DataMap.SimpleDataKey<BooleanModel> BOOLEAN_MODEL
        = DataMap.simpleDataKey("pmd.dataflow.val.boolean");

    public static ValueAnalysisResult process(ASTCompilationUnit acu) {
        AnalysisEngine engine = new AnalysisEngine();
        engine.register(new NullabilityAnalysis());
        engine.register(new BooleanValueAnalysis());

        for (ASTTypeDeclaration typeDecl : acu.getTypeDeclarations()) {
            ValueAnalysisState subResult = new ValueAnalysisState(engine);
            BaseDataflowPass.processTypeDecl(typeDecl, subResult);

        }

        return new ValueAnalysisResult();
    }

    private static class ValueAnalysisState extends BaseDataflowPass.GlobalAlgoState {

        private final AnalysisEngine engine;
        private final BooleanValueAnalysis boolAnalysis;

        public ValueAnalysisState(AnalysisEngine engine) {
            this.engine = engine;
            boolAnalysis = engine.getAnalysis(BooleanValueAnalysis.class);
        }

        @Override
        public BooleanModel speculateCondition(@NonNull ASTExpression expr, DataflowScope scope) {
            return scope.getModel(expr, boolAnalysis);
        }

        @Override
        public void setConditionSpec(ASTExpression expr, BooleanModel spec, DataflowScope scope) {
            scope.setModel(expr, spec, boolAnalysis);
        }
    }

    public static class ValueAnalysisResult {

        /**
         * Return the nullability of an expression.
         */
        public Nullability getNullability(ASTExpression expr) {
            // todo maybe cache the entire dataflowscope it would
            //  be less fragmented I guess.
            return expr.getUserMap().getOrDefault(NULLABILITY_RESULT, Nullability.UNKNOWN);
        }

        public BooleanModel getBooleanModel(ASTExpression expr) {
            return expr.getUserMap().getOrDefault(BOOLEAN_MODEL, BooleanModel.UNKNOWN);
        }
    }
}
