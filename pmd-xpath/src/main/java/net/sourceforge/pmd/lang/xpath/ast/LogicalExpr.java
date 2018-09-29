/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * A logical expression is either an and-expression or an or-expression.
 * If a logical expression does not raise an error, its value is always one
 * of the boolean values true or false.
 *
 * <pre>
 *
 * LogicalExpr ::= {@linkplain ASTOrExpr OrExpr} | {@linkplain ASTAndExpr AndExpr}
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface LogicalExpr extends ExprSingle {
}
