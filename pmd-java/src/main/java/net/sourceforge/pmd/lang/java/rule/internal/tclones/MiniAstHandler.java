/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 * Extension point to configure of the mini tree is produced from an AST.
 *
 * @param <N> Type of ast node
 */
interface MiniAstHandler<N extends GenericNode<N>> {

    /** True to remove the node and its whole subtree from the minitree. */
    default boolean isIgnored(N node) {
        return false;
    }

    // todo commutative sequences

    /** True to consider that the children of this node are in a sequence. */
    default boolean isSequencer(N node) {
        return false;
    }

    /**
     * Add attributes of the node to the builder. If two nodes have the
     * same {@link #getRuleKind(GenericNode)}, then they must be added
     * the exact same number of attributes in the same order, otherwise
     * they won't ever be equal.
     *
     * <p>Some attributes count in the structural phase, others only
     * count towards the similarity score.
     */
    void hashAttributes(N node, MiniTreeBuilder builder);

    /**
     * An integer that identifies a "class" of nodes, which can be equal
     * together, and declare the same attributes. The default uses the
     * node's class.
     */
    default int getRuleKind(N node) {
        return node.getClass().hashCode();
    }
}
