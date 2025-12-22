/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

/**
 * A value model models the statically known state of a value in the
 * program. It must support a lattice structure, ie, two models of the
 * same type can always be joined into a new model that represents the
 * union of the state represented by both models.
 *
 * @param <T> Self type
 */
public interface ValueModel<T extends ValueModel<T>> {

    /**
     * Produce a model that represents the union of both sets of facts.
     * This must behave as the join operator of a lattice, meaning it
     * must have the following properties:
     * <ul>
     * <li>Commutativity: {@code A.join(B) == B.join(A)};
     * <li>Associativity: {@code A.join(B).join(C) == A.join(B.join(C))};
     * <li>Reflexivity: {@code A.join(A) == A}.
     * </ul>
     * Additionally, it must support two designated elements, {@linkplain #isTop() top}
     * and {@linkplain #isBottom()}.
     *
     * @param other Another model
     */
    T join(T other);

    /**
     * Return true if this is the top of the lattice. This represents
     * a state where no specific assumptions about the value can be made,
     * an "unknown" state. It is an absorbing element, i.e.
     * {@code top.join(X) = top} for all {@code X}.
     */
    boolean isTop();

    // todo seems we could do away with the bottom value and just represent
    //  that using absence.

    /**
     * Return true if this is the bottom of the lattice. This represents
     * the state of an empty local variable, i.e., there is no value to
     * model. All value models must support a designated bottom element.
     * It is a null element, i.e. {@code bottom.join(X) = X} for all {@code X}.
     */
    boolean isBottom();

}
