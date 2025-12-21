/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULL;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.AnalysisEngine;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.DataflowPass;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis;

public class NullabilityProblemRule extends AbstractJavaRule {


    @Override
    public Object visit(ASTCompilationUnit node, Object data) {

        AnalysisEngine engine = DataflowPass.newAnalysisEngine(node);
        NullabilityAnalysis analysis = new NullabilityAnalysis();
        engine.register(analysis);

        node.descendants(ASTExpression.class).crossFindBoundaries()
            .forEach(it -> {
                if (it instanceof ASTVariableAccess
                    && !JavaAstUtils.isVarAccessStrictlyWrite((ASTVariableAccess) it)
                    && analysis.getNullability(it) == NULL) {
                    asCtx(data).addViolationWithMessage(it, "Variable `{0}` is always null", ((ASTVariableAccess) it).getName());
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
