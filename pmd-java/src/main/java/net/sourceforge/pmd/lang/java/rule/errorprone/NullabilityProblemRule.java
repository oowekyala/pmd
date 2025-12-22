/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueAnalysisFacade.ValueAnalysisResult;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueAnalysisFacade;
import net.sourceforge.pmd.util.OptionalBool;

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
                } else if (expressionWillNpeIfNull(it) && nullability.mayBeNull() == OptionalBool.YES) {
                    if (nullability == NULL) {
                        asCtx(data).addViolationWithMessage(it, "Expression will NPE at runtime");
                    } else {
                        asCtx(data).addViolationWithMessage(it, "Expression may NPE at runtime");
                    }
                }
            });
        return null;
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
