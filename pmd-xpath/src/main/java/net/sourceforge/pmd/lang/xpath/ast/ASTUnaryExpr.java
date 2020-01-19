/*
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
public final class ASTUnaryExpr extends AbstractXPathExpr implements Expr {

    /** Constructor for synthetic node. */
    public ASTUnaryExpr() {
        super(XPathParserImplTreeConstants.JJTUNARYEXPR);
    }

    ASTUnaryExpr(int id) {
        this();
    }

    private XpUnaryOp operator;


    void setOp(XpUnaryOp operator) {
        this.operator = operator;
    }


    public XpUnaryOp getOperator() {
        return operator;
    }


    public Expr getOperand() {
        return (Expr) getChild(0);
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
