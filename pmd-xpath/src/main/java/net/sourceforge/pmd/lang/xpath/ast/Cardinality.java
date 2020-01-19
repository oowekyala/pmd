/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Cardinality of a {@linkplain ASTSequenceType sequence type}.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
enum Cardinality {
    /** Cardinality marked by the "?" occurrence indicator. */
    ZERO_OR_ONE("?"),
    /** Cardinality marked by the "*" occurrence indicator. */
    ZERO_OR_MORE("*"),
    /** Cardinality assumed when no occurrence indicator is present. */
    EXACTLY_ONE(""),
    /** Cardinality marked by the "+" occurrence indicator. */
    ONE_OR_MORE("+");


    private final String occurrenceIndicator;


    Cardinality(String occurrenceIndicator) {
        this.occurrenceIndicator = occurrenceIndicator;
    }


    /**
     * Returns the occurrence indicator of this cardinality.
     * If this is {@link #EXACTLY_ONE}, then returns the empty string.
     */
    public String getOccurrenceIndicator() {
        return occurrenceIndicator;
    }

}
