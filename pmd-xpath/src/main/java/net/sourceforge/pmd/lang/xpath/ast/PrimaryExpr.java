/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Marker for primary expressions. Primary expressions have the highest
 * precedence.
 *
 * <pre>
 *
 * PrimaryExpr ::= {@link ASTNumericLiteral NumericLiteral}
 *               | {@link ASTStringLiteral StringLiteral}
 *               | {@link ASTVarRef VarRef}
 *               | {@link ASTContextItemExpr ContextItemExpr}
 *               | {@link ASTFunctionCall FunctionCall}
 *               | {@link FunctionItemExpr} )
 *
 * </pre>
 */
public interface PrimaryExpr extends Expr, StepExpr {
}
