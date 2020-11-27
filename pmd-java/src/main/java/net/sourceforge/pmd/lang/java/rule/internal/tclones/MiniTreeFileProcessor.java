/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.TextAvailableNode;
import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;
import net.sourceforge.pmd.util.document.Locator;

/**
 * Proxy to a {@link CloneDetectorGlobals} scoped to a single file.
 */
final class MiniTreeFileProcessor<N extends GenericNode<N> & TextAvailableNode> {

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

        MiniTreeBuilder childrenBuilder = myBuilder.childrenBuilder();

        // builder may be reset and reused for all children
        for (N child : node.children()) {
            if (ast.isIgnored(child)) {
                continue;
            }
            MiniTree childTree = addSubtreesRecursively(child, childrenBuilder);
            myBuilder.addChild(childTree); // add children hash to this hash too
        }

        ast.hashAttributes(node, myBuilder);

        MiniTree built = myBuilder.buildAndReset(node.getTextRegion());
        globals.acceptTree(built);
        return built;
    }
}
