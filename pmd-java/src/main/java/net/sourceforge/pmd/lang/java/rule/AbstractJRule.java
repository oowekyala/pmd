/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.AstProcessingStage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaProcessingStage;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.SideEffectingVisitorAdapter;
import net.sourceforge.pmd.rule7.RuleImpl.JvmRuleImpl;

public class AbstractJRule extends SideEffectingVisitorAdapter<RuleContext> implements JvmRuleImpl {


    @Override
    public void visit(JavaNode node, RuleContext data) {
        visitChildren(node, data);
    }

    protected void visitChildren(JavaNode node, RuleContext data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
    }

    @Override
    public void visit(Node node, RuleContext ctx) {
        ((JavaNode) node).jjtAccept(this, ctx);
    }

    @Override
    public boolean dependsOn(AstProcessingStage<?> stage) {
        if (!(stage instanceof JavaProcessingStage)) {
            throw new IllegalArgumentException("Processing stage wasn't a Java one: " + stage);
        }
        return ((JavaProcessingStage) stage).ruleDependsOnThisStage(this);
    }

}
