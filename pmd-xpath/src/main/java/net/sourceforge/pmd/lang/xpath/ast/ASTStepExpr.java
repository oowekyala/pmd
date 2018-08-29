/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Step of a {@linkplain ASTPathExpr path expression}.
 *
 * <pre>
 *
 * StepExpr ::= {@linkplain ASTPostfixExpr PostfixExpr} | {@linkplain ASTAxisStep AxisStep}
 *
 * </pre>
 */
public final class ASTStepExpr extends AbstractXPathNode {


    ASTStepExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Returns true if this step is a postfix expression,
     * i.e. not an axis step.
     */
    public boolean isPostfixExpr() {
        return !isAxisStep();
    }


    /**
     * Returns true if this step is an axis step, i.e.
     * not a postfix expr.
     */
    public boolean isAxisStep() {
        return jjtGetChild(0) instanceof ASTAxisStep;
    }
}
/* JavaCC - OriginalChecksum=7ace41bad368a1c58ad4264392088d8d (do not edit this line) */
