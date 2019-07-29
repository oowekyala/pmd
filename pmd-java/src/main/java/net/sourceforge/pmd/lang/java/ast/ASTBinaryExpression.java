/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a binary expression.
 *
 * TODO ConditionalAndExpression and ConditionalOrExpression are also binary, those are the only ones that remain...
 * TODO instanceof also is also kindof binary, we need to introduce a TypeAccess node to give an expression API to a type node
 *  (that's what spoon does)
 */
public interface ASTBinaryExpression extends ASTExpression, JSingleChildNode<ASTExpression>, LeftRecursiveNode {


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
