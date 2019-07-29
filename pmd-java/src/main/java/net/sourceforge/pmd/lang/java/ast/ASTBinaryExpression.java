/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

public interface ASTBinaryExpression extends ASTExpression, JSingleChildNode<ASTExpression> {


    /** Returns the operator. */
    BinaryOp getOperator();


    /** Returns the left-hand-side operand. */
    default ASTExpression getLhs() {
        return jjtGetChild(0);
    }


    /** Returns the right-hand-side operand. */
    default ASTExpression getRhs() {
        return jjtGetChild(1);
    }
}
