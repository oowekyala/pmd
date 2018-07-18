/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Cardinality of e.g. a type.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
enum Cardinality {
    ZERO_OR_ONE, ZERO_OR_MORE, ONE_OR_MORE;
}
