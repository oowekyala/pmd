/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;


import java.io.IOException;

import net.sourceforge.pmd.document.ReplaceHandler;

/**
 * TODO
 *  * Memory leaks (node may be captured by fix, holding off all the tree + tokens + doc)
 *  * Is passing a handler good enough? we could also pass a MutableDocument
 *   to let tree edit operations destroy other tokens
 *  * Act on nodes, not directly text
 *  * Conflicting/overlapping fixes?
 *  * Should we always return an array of patches anyway?
 *  * Applying the fix should be aborted if there were file-system changes
 *
 */
public interface Autofix {

    <T> T apply(ReplaceHandler<T> handler) throws IOException;

}
