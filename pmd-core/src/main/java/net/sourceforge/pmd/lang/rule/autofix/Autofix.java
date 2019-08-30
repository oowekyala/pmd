/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;


import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;

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
public interface Autofix<N extends TextAvailableNode, T extends GenericToken> {

    AutofixImpl<N, T> getImpl();


    String getDescription();


    LanguageVersion getLanguageVersion();


    static <N extends TextAvailableNode, T extends GenericToken> Autofix<N, T> from(String description, LanguageVersion version, AutofixImpl<N, T> impl) {
        return new Autofix<N, T>() {
            @Override
            public AutofixImpl<N, T> getImpl() {
                return impl;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public LanguageVersion getLanguageVersion() {
                return version;
            }
        };
    }


    @FunctionalInterface
    interface AutofixImpl<N extends TextAvailableNode, T extends GenericToken> {

        void apply(TreeEditSession<N, T> session);

    }

}
