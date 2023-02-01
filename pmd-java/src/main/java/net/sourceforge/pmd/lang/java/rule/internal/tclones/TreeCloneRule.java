/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import static net.sourceforge.pmd.properties.constraints.NumericConstraints.inRange;
import static net.sourceforge.pmd.properties.constraints.NumericConstraints.positive;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.document.Locator;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * A rule that implements tree clone detection as implemented by this package.
 * This is a global rule that may be memory intensive, as it
 * stores a minified copy of each AST in the processed files.
 * TODO like other global rules it does not support incremental analysis.
 */
public class TreeCloneRule extends AbstractJavaRulechainRule {

    private static CloneDetectorGlobals globalState;


    private static final PropertyDescriptor<Integer> MASS_THRESHOLD =
        PropertyFactory.intProperty("massThreshold")
            .desc("Minimum mass for a subtree to be considered, in number of nodes.")
            .defaultValue(30)
            .require(positive())
            .build();

    private static final PropertyDescriptor<Integer> SIMILARITY_THRESHOLD =
        PropertyFactory.intProperty("simThreshold")
                       .desc("Minimum similarity score for clones to be reported (similarity ranges between 0 and 1000).")
                       .defaultValue(900)
                       .require(inRange(1, 1000))
                       .build();

    private MiniTreeFileProcessor<JavaNode> processor;

    public TreeCloneRule() {
        super(ASTCompilationUnit.class);
        definePropertyDescriptor(MASS_THRESHOLD);
        definePropertyDescriptor(SIMILARITY_THRESHOLD);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        if (globalState == null) {
            // this should be placed in rule initialization block.
            globalState = new CloneDetectorGlobals(
                getProperty(MASS_THRESHOLD),
                getProperty(SIMILARITY_THRESHOLD) / 1000.0
            );
        }
        if (processor == null) {
            Locator loc = node.getTextDocument().detachLocator();
            processor = new MiniTreeFileProcessor<>(globalState, loc, JavaMiniTreeAttributes.JAVA_CONFIG);
        }
        processor.addSubtreesRecursively(node);
        return null;
    }

    @Override
    public void end(RuleContext ctx) {
        processor = null;
        globalState.endFile();
    }

    @Override
    public void endAnalysis(RuleContext ctx) {
        // TODO this is a prototype and does not yet report
        // violations through the rule context.
        globalState.computeDuplicates();
    }
}
