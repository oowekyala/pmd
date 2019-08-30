/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;

import java.util.List;

import net.sourceforge.pmd.document.patching.TextPatch;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;

/**
 * Records changes to a tree as a list of actions to apply.
 * The tree is not directly changed, for example, a {@link #delete(TextAvailableNode) deletion}
 * operation does not change the number of children of the parent.
 */
public interface TreeEditSession<N extends TextAvailableNode, T extends GenericToken> {


    TokenEditSession<T> getTokenEditor();


    TextPatch commit();


    /**
     * Delete a node from the tree.
     *
     * @throws UnsupportedOperationException If the tree is left in an inconsistent state
     */
    void delete(N node);


    /**
     * Replace a node in the tree.
     *
     * @throws ClassCastException If the replacement cannot replace the node because of its type
     */
    void replace(N node, N replacement);


    interface TokenEditSession<T extends GenericToken> {

        /** Delete a token. */
        void deleteToken(T tok);

    }

}
