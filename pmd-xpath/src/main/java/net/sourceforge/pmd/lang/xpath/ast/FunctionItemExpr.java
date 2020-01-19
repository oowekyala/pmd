/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Expression that evaluates to a function value.
 * One of the primary expressions.
 *
 * <pre>
 *
 * FunctionItemExpr ::= {@linkplain ASTNamedFunctionRef NamedFunctionRef}
 *                    | {@linkplain ASTInlineFunctionExpr InlineFunctionExpr}
 *
 * </pre>
 */
public interface FunctionItemExpr extends PrimaryExpr {
}
