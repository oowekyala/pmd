/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.TextAvailableNode;
import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.document.Locator;
import net.sourceforge.pmd.lang.document.TextRegion;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 * Proxy to a {@link CloneDetectorGlobals} scoped to a single file.
 */
final class MiniTreeFileProcessor<N extends GenericNode<N> & TextAvailableNode> {

    private static final int SEQUENCE_FAKE_KIND = -495062;
    private final CloneDetectorGlobals globals;
    private final Locator locator;
    private final MiniAstHandler<N> ast;
    private final Set<N> roots;

    MiniTreeFileProcessor(CloneDetectorGlobals globals, Locator locator, MiniAstHandler<N> ast) {
        this.globals = globals;
        this.locator = locator;
        this.ast = ast;
        this.roots = new HashSet<>();
    }


    public void addSubtreesRecursively(N root) {
        if (roots.add(root)) {
            addSubtreesRecursively(root, new MiniTreeBuilder(locator));
        }
    }

    private MiniTree addSubtreesRecursively(N node, MiniTreeBuilder myBuilder) {
        myBuilder.hashKind(ast.getRuleKind(node));

        // builder may be reset and reused for all children
        MiniTreeBuilder childrenBuilder = newBuilder();

        if (ast.isSequencer(node)) {
            buildSequence(node, myBuilder, childrenBuilder);
        } else {
            buildNormal(node, myBuilder, childrenBuilder);
        }

        ast.hashAttributes(node, myBuilder);

        MiniTree built = myBuilder.buildAndReset(node.getTextRegion());
        globals.acceptTree(built);
        return built;
    }

    private void buildNormal(N node, MiniTreeBuilder myBuilder, MiniTreeBuilder childrenBuilder) {
        for (N child : node.children()) {
            if (ast.isIgnored(child)) {
                continue;
            }
            MiniTree childTree = addSubtreesRecursively(child, childrenBuilder);
            myBuilder.addChild(childTree); // add children hash to this hash too
        }
    }


    public MiniTreeBuilder newBuilder() {
        return new MiniTreeBuilder(this.locator);
    }

    private void buildSequence(N node, MiniTreeBuilder myBuilder, MiniTreeBuilder childrenBuilder) {
        if (node.getNumChildren() == 0) {
            return;
        }
        /*
        This builds a tree that is unbalanced to the left
         */

        MiniTreeBuilder tmpBuilder = newBuilder();
        tmpBuilder.hashKind(SEQUENCE_FAKE_KIND);

        MiniTree left = addSubtreesRecursively(node.getChild(0), childrenBuilder);

        for (int i = 1; i < node.getNumChildren(); i++) {
            MiniTree right = addSubtreesRecursively(node.getChild(i), childrenBuilder);
            tmpBuilder.addChild(left);
            tmpBuilder.addChild(right);

            TextRegion combinedRegion = TextRegion.union(left.getRegion(), right.getRegion());
            left = tmpBuilder.buildAndReset(combinedRegion);
            tmpBuilder.hashKind(SEQUENCE_FAKE_KIND);
        }

        myBuilder.addChild(left);
    }
}
