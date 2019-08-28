/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.ast.AstProcessingStage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaProcessingStage;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.SideEffectingVisitorAdapter;
import net.sourceforge.pmd.lang.rule.internal.TargetSelectionStrategy;
import net.sourceforge.pmd.lang.rule.internal.TargetSelectionStrategy.ClassRulechainVisits;
import net.sourceforge.pmd.rule7.RuleImpl.JvmRuleImpl;
import net.sourceforge.pmd.rule7.ScopedRuleContext;

public abstract class AbstractJRule extends SideEffectingVisitorAdapter<ScopedRuleContext> implements JvmRuleImpl {


    @Override
    public void visit(JavaNode node, ScopedRuleContext data) {
        visitChildren(node, data);
    }

    protected void visitChildren(JavaNode node, ScopedRuleContext data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ((JavaNode) node.jjtGetChild(i)).jjtAccept(this, data);
        }
    }

    @Override
    public TargetSelectionStrategy getTargetingStrategy() {
        return ClassRulechainVisits.ROOT_ONLY;
    }

    @Override
    public void apply(Node node, ScopedRuleContext ctx) {
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
