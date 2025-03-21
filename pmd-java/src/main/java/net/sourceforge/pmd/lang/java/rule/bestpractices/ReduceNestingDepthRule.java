/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.metrics.Metric;
import net.sourceforge.pmd.lang.metrics.MetricsUtil;

public class ReduceNestingDepthRule extends AbstractJavaRulechainRule {

    public ReduceNestingDepthRule() {
        super(ASTBodyDeclaration.class);
    }

    @Override
    public Object visit(ASTIfStatement ifStmt, Object data) {
        if (ifStmt.getElseBranch() == null
            || !isEasilyNegatable(ifStmt.getCondition())) {
            return null;
        }

        int complexityOfFollowers = getComplexity(ifStmt.asStream().followingSiblings());
        int complexityOfThen = getComplexity(ifStmt.getThenBranch());
        if (areMeaningFullyDifferent(complexityOfFollowers, complexityOfThen)) {
            recommendInvertingIfCondition(ifStmt);
        }
        return null;
    }

    private static final Metric<ASTStatement, Integer> VISUAL_COMPLEXITY =
        Metric.of(
            (node, options) -> computeVisualComplexity(node),
            node -> node instanceof ASTStatement ? (ASTStatement) node : null,
            "Visual Complexity"
        );

    static int computeVisualComplexity(ASTStatement stmt) {
        if (stmt == null) {
            return 0;
        }
        return stmt.acceptVisitor(new VisualComplexityBuilder(), null);
    }

    private int getComplexity(NodeStream<?> nodes) {
        int acc = 0;
        for (ASTStatement node : nodes.filterIs(ASTStatement.class)) {
            acc += MetricsUtil.computeMetric(VISUAL_COMPLEXITY, node);
        }
        return acc;
    }


    static class VisualComplexityBuilder extends JavaVisitorBase<Void, Integer> {

        @Override
        public Integer visitStatement(ASTStatement node, Void data) {


        }

        @Override
        public Integer visit(ASTIfStatement node, Void data) {
            ASTStatement thenBranch = node.getThenBranch();
            ASTStatement elseBranch = node.getElseBranch();
            int complexity = MetricsUtil.computeMetric(VISUAL_COMPLEXITY, thenBranch);

            return super.visit(node, data);
        }
    }

}
