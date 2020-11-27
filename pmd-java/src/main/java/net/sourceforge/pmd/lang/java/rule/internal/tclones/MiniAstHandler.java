/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 *
 */
interface MiniAstHandler<N extends GenericNode<N>> {

    default boolean isIgnored(N node) {
        return false;
    }

    default boolean isSequencer(N node) {
        return false;
    }

    void hashAttributes(N node, MiniTreeBuilder builder);

    default int getRuleKind(N node) {
        return node.getClass().hashCode();
    }
}
