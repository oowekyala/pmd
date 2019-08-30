/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;

import net.sourceforge.pmd.document.patching.TextPatch;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;

/**
 * This is the context object for the application of an autofix.
 * This interface provides methods to mutate ASTs, it abstracts what's
 * really happening from the autofix implementations.
 *
 * <p>The current implementation translates operation on trees (eg deleting a node)
 * to operations on a document (eg deleting some text region). Those text operations
 * are recorded as a text patch, which is then presented to the user to
 * check if it's ok.
 *
 * <p>For now this is the only way to detach the autofix from the AST and
 * let the AST and its context be garbage collected. But it's incredibly
 * restrictive. The AST is never directly changed, for example, a {@link #delete(TextAvailableNode) deletion}
 * operation does not change the number of children of the parent.
 */
public interface TreeEditSession<N extends TextAvailableNode> {

    /**
     * End the session and returns the aggregated text patch.
     * This should not be called from autofix implementations.
     */
    TextPatch commit();

    /*
        This prototype only supports deleting nodes (and just type bounds)
     */


    /** Delete a node from the tree. */
    void delete(N node);


}
