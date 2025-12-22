/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULLABLE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueAnalysisFacade.ValueAnalysisResult;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueAnalysisFacade;

public class NullabilityProblemRule extends AbstractJavaRule {


    @Override
    public Object visit(ASTCompilationUnit node, Object data) {

        ValueAnalysisResult analysis = ValueAnalysisFacade.process(node);

        node.descendants(ASTExpression.class).crossFindBoundaries()
            .forEach(it -> {
                NullabilityAnalysis.Nullability nullability = analysis.getNullability(it);
                if (it instanceof ASTVariableAccess
                    && !JavaAstUtils.isVarAccessStrictlyWrite((ASTVariableAccess) it)
                    && nullability == NULL) {
                    asCtx(data).addViolationWithMessage(it, "Variable `{0}` is always null", ((ASTVariableAccess) it).getName());
                } else if (expressionWillNpeIfNull(it)) {
                    if (nullability == NULL) {
                        asCtx(data).addViolationWithMessage(it, "Expression will NPE at runtime");
                    } else if (nullability == NULLABLE) {
                        asCtx(data).addViolationWithMessage(it, "Expression may NPE at runtime");
                    }
                }
                if (isStatementCondition(it)) {
                    BooleanModel booleanModel = analysis.getBooleanModel(it);
                    if (booleanModel == BooleanModel.TRUE || booleanModel == BooleanModel.FALSE) {
                        String boolAsString = booleanModel == BooleanModel.TRUE ? "true" : "false";
                        asCtx(data).addViolationWithMessage(it, "Condition is always {0}", boolAsString);
                    }
                }
            });
        return null;
    }

    private static boolean isStatementCondition(ASTExpression it) {
        return it.getParent() instanceof ASTStatement;
    }

    boolean expressionWillNpeIfNull(ASTExpression e) {
        if (e.getParent() instanceof ASTPrimaryExpression && e.getIndexInParent() == 0) {
            // e.method()
            // e.field
            // e::method
            // e[index]
            return true;
        } else if (e.getParent() instanceof ASTSwitchExpression) { // todo null tolerance
            return true;
        }
        return false;
    }

}
