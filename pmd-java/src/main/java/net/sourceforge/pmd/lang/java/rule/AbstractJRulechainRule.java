/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.rule;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

public abstract class AbstractJRulechainRule extends AbstractJRule {

    public AbstractJRulechainRule() {

    }

    @Override
    public void visit(JavaNode node, RuleContext data) {
        // do not recurse
    }

}
