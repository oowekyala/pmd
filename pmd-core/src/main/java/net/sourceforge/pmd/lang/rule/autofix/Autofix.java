/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.autofix;


import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;

/**
 * An automated fix for a rule violation.
 * TODO
 *  * Memory leaks (node may be captured by fix, holding off all the tree + tokens + doc)
 *  * Conflicting/overlapping fixes?
 *  * Applying the fix should be aborted if there were file-system changes
 *
 */
public interface Autofix<N extends TextAvailableNode> {

    AutofixImpl<N> getImpl();


    String getDescription();


    LanguageVersion getLanguageVersion();


    static <N extends TextAvailableNode> Autofix<N> from(String description, LanguageVersion version, AutofixImpl<N> impl) {
        return new Autofix<N>() {
            @Override
            public AutofixImpl<N> getImpl() {
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


    /**
     * This apply method is kept separate from the Autofix interface to
     * allow writing a lambda.
     */
    @FunctionalInterface
    interface AutofixImpl<N extends TextAvailableNode> {

        void apply(TreeEditSession<N> session);

    }

}
