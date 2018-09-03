/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Marker for all expressions except {@linkplain ASTSequenceExpr SequenceExpr}.
 * SequenceExpr has the lowest priority of all, and is forbidden in some contexts.
 *
 * <pre>
 *
 * ExprSingle ::= {@linkplain ASTForExpr ForExpr}
 *              | {@linkplain ASTLetExpr LetExpr}
 *              | {@linkplain ASTQuantifiedExpr QuantifiedExpr}
 *              | {@linkplain ASTIfExpr IfExpr}
 *              | {@linkplain ASTOrExpr OrExpr}
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface ExprSingle extends Expr {
}
