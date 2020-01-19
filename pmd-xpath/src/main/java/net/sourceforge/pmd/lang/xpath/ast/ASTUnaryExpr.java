/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

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
        super(XPathParserImplTreeConstants.JJTUNARYEXPR);
    }
    private String operator;


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
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


}
/* JavaCC - OriginalChecksum=1036550861161e650ddf9b4917bae7c1 (do not edit this line) */
