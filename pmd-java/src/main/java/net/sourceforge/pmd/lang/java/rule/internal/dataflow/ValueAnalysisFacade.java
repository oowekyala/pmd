/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.*;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.util.DataMap;

public class ValueAnalysisFacade {

    private static final DataMap.SimpleDataKey<Nullability> NULLABILITY_RESULT
        = DataMap.simpleDataKey("pmd.dataflow.nullability");

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

        public ValueAnalysisState(AnalysisEngine engine) {
            this.engine = engine;
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
    }
}
