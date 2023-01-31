/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.document.Locator;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;

/**
 *
 */
public class TreeCloneRule extends AbstractJavaRulechainRule {

    private static final int MIN_MASS = 30;
    private static final double SIM_THRESHOLD = 0.9;
    private static final CloneDetectorGlobals STATE = new CloneDetectorGlobals(MIN_MASS, SIM_THRESHOLD);

    private MiniTreeFileProcessor<JavaNode> processor;

    public TreeCloneRule() {
        super(ASTCompilationUnit.class);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        if (processor == null) {
            Locator loc = node.getTextDocument().detachLocator();
            processor = new MiniTreeFileProcessor<>(STATE, loc, JavaMiniTreeAttributes.JAVA_CONFIG);
        }
        processor.addSubtreesRecursively(node);
        return null;
    }

    @Override
    public void end(RuleContext ctx) {
        processor = null;
        STATE.endFile();
    }

    @Override
    public void endAnalysis(RuleContext ctx) {
        STATE.computeDuplicates();
    }
}
