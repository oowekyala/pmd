/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Conditional expression.
 *
 * <pre>
 *
 * IfExpr ::= "if" "(" {@link Expr} ")" "then" {@link ExprSingle} "else" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTIfExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTIfExpr() {
        super(null, XPathParserTreeConstants.JJTIFEXPR);
    }


    ASTIfExpr(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the node that represents the guard of this conditional.
     */
    public Expr getGuardExpressionNode() {
        return (Expr) jjtGetChild(0);
    }


    /**
     * Returns the node that represents the expression that will be evaluated
     * if the guard evaluates to true.
     */
    public ExprSingle getTrueAlternative() {
        return (ExprSingle) jjtGetChild(1);
    }


    /**
     * Returns the node that represents the expression that will be evaluated
     * if the guard evaluates to false.
     */
    public ExprSingle getFalseAlternative() {
        return (ExprSingle) jjtGetChild(2);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=bc4bcc1f2a1daa67c83fae268966da0d (do not edit this line) */
