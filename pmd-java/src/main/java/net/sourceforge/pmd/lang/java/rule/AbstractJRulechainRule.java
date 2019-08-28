/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.rule;

import java.util.Set;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.rule.internal.TargetSelectionStrategy;
import net.sourceforge.pmd.lang.rule.internal.TargetSelectionStrategy.ClassRulechainVisits;
import net.sourceforge.pmd.rule7.ScopedRuleContext;

public abstract class AbstractJRulechainRule extends AbstractJRule {

    protected AbstractJRulechainRule() {

    }

    @Override
    public void visit(JavaNode node, ScopedRuleContext data) {
        // do not recurse
    }


    protected abstract Set<? extends Class<? extends Node>> getRulechainVisits();


    @Override
    public TargetSelectionStrategy getTargetingStrategy() {
        return new ClassRulechainVisits(getRulechainVisits());
    }
}
