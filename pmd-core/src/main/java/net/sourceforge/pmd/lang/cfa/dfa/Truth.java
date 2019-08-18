/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.dfa;


import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Something true about something in the program.
 */
public interface Truth<N> {

    /** The variable this truth refers to. */
    @Nullable
    N getVariable();


    /** Returns a description of this truth, in a concise way. */
    @Override
    String toString();


    TruthKind<N> getKind();


    /**
     * Returns true iff the object is a {@link Truth} of the same kind,
     * with the same {@linkplain #getVariable() variable}.
     */
    @Override
    boolean equals(Object o);


    /** Instance must be shared by all truths of the same kind. */
    interface TruthKind<N> {

        /** Reduce the given set of truths into a new set. */
        Set<? extends Truth<N>> reduce(Map<TruthKind<N>, ? extends Truth<N>> truths);

    }
}
