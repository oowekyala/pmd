/**
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
    /** This is the cardinality of the empty-sequence() type. */
    EMPTY,
    /** Cardinality marked by the "?" occurrence indicator. */
    ZERO_OR_ONE,
    /** Cardinality marked by the "*" occurrence indicator. */
    ZERO_OR_MORE,
    /** Cardinality assumed when no occurrence indicator is present. */
    EXACTLY_ONE,
    /** Cardinality marked by the "+" occurrence indicator. */
    ONE_OR_MORE;
}
