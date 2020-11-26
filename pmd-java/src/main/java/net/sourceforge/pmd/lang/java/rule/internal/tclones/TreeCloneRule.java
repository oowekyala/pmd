/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.util.document.Locator;

/**
 *
 */
public class TreeCloneRule extends AbstractJavaRulechainRule {

    private static final CloneDetectorGlobals STATE = new CloneDetectorGlobals(10);

    private Locator loc;

    public TreeCloneRule() {
        super(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (loc == null) {
            loc = node.getTextDocument().detachLocator();
        }
        ASTBlock body = node.getBody();
        if (body != null) {
            MiniTreeBuilderVisitor.buildJavaMiniTree(body, STATE, loc);
        }
        return null;
    }

    @Override
    public void end(RuleContext ctx) {
        loc = null;
        STATE.endFile();
    }

    @Override
    public void endAnalysis(RuleContext ctx) {
        STATE.computeDuplicates();
    }
}
