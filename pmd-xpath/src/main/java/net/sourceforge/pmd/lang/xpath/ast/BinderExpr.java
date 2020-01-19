/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Objects;

import net.sourceforge.pmd.lang.ast.NodeStream;


/**
 * Expression binding one or more variables to a value.
 *
 * <pre>
 *
 * BinderExpr ::= {@linkplain ASTForExpr ForExpr} | {@linkplain ASTLetExpr LetExpr} | {@linkplain ASTQuantifiedExpr QuantifiedExpr}
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface BinderExpr extends Expr {


    /**
     * Returns a node stream of the bindings declared in this expression.
     */
    default NodeStream<ASTVarBinding> getBindings() {
        return children(ASTVarBinding.class);
    }


    /**
     * Returns the expression in which the bound variables are
     * in scope. Variables are also in scope in the initializers
     * of variables declared after themselves.
     */
    default Expr getBodyExpr() {
        return (Expr) Objects.requireNonNull(getLastChild());
    }

}
