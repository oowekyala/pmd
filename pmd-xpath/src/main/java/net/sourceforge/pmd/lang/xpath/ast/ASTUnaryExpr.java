/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Unary prefix expression. The sequence of "+" and "-" is concatenated
 * into a string, available from {@link #getOperator()}.
 *
 * <pre>
 *
 * UnaryExpr ::= ("+" | "-")+  {@linkplain ASTMapExpr MapExpr}
 *
 * </pre>
 */
public final class ASTUnaryExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTUnaryExpr() {
        super(null, XPathParserTreeConstants.JJTUNARYEXPR);
    }
    private String operator;


    ASTUnaryExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setOperator(String operator) {
        this.operator = operator;
    }


    public String getOperator() {
        return operator;
    }


    public ExprSingle getOperand() {
        return (ExprSingle) jjtGetChild(0);
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
/* JavaCC - OriginalChecksum=1036550861161e650ddf9b4917bae7c1 (do not edit this line) */
