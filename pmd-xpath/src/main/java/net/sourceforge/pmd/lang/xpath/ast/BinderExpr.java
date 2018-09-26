/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Collections;
import java.util.List;


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
public interface BinderExpr extends ExprSingle {


    /**
     * Returns an unmodifiable list of the bindings declared in this expression.
     */
    default List<ASTVarBinding> getBindings() {
        return Collections.unmodifiableList(findChildrenOfType(ASTVarBinding.class));
    }


    /**
     * Returns the expression in which the bound variables are
     * in scope. Variables are also in scope in the initializers
     * of variables declared after themselves.
     */
    default ExprSingle getBodyExpr() {
        return (ExprSingle) getLastChild();
    }

}
