/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

/** A token type that is also linked to the previous token. */
public interface PrevLinkedToken<T extends PrevLinkedToken<T>> extends GenericToken<T> {

    /** Returns the previous normal type token. Returns null if this is the first token. */
    @Nullable T getPrevious();


    @Override
    @Nullable T getPreviousComment();

}
