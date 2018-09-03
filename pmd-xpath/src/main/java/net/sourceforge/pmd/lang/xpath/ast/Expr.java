/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Root interface for all expressions. Groups {@link ExprSingle}
 * and {@linkplain ASTSequenceExpr SequenceExpr} together.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface Expr extends XPathNode {
}
