/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTreeBuilderVisitor.CloneDetectorGlobals;

/**
 *
 */
public class TreeCloneRule extends AbstractJavaRulechainRule {
    private static final CloneDetectorState STATE = new CloneDetectorState(20);

    public TreeCloneRule() {
        super(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        ASTBlock body = node.getBody();
        if (body != null) {
            MiniTreeBuilderVisitor.buildJavaMiniTree(body, STATE);
        }
        return null;
    }

    static final class CloneDetectorState implements CloneDetectorGlobals {

        final ConcurrentMap<Integer, List<MiniTree>> buckets = new ConcurrentHashMap<>();
        final int minConsideredSize;

        CloneDetectorState(int minConsideredSize) {
            this.minConsideredSize = minConsideredSize;
        }

        @Override
        public void acceptTree(MiniTree tree) {
            if (tree.mass() >= minConsideredSize) {
                // `compute` guarantees atomic access to the list so it doesn't need synchronization
                buckets.compute(tree.hash(), (key, list) -> {
                    if (list == null) {
                        list = new ArrayList<>(1);
                    }
                    list.add(tree);
                    return list;
                });
            }
        }
    }
}
