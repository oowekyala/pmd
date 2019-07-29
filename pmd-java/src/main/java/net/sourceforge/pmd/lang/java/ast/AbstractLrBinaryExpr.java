/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Base class for some expressions that are parsed left-recursively.
 *
 * @author Clément Fournier
 */
abstract class AbstractLrBinaryExpr extends AbstractJavaExpr implements ASTBinaryExpression {

    private BinaryOp operator;

    AbstractLrBinaryExpr(int i) {
        super(i);
    }

    AbstractLrBinaryExpr(JavaParser p, int i) {
        super(p, i);
    }


    @Override
    public ASTExpression jjtGetChild(int index) {
        return (ASTExpression) super.jjtGetChild(index);
    }

    @Override
    public void setImage(String image) {
        super.setImage(image);
        this.operator = BinaryOp.fromImage(image);
    }

    /**
     * Returns the operator.
     */
    @Override
    public BinaryOp getOperator() {
        return operator;
    }
}
