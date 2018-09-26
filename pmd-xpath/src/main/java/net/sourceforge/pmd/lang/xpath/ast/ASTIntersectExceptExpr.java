/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Intersect or except expression.
 *
 * <pre>
 *
 * IntersectExceptExpr ::= {@linkplain ASTInstanceofExpr InstanceofExpr} ({@linkplain ASTIntersectExceptOperator IntersectExceptOperator} {@linkplain ASTInstanceofExpr InstanceofExpr})+
 *
 * </pre>
 */
public final class ASTIntersectExceptExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTIntersectExceptExpr() {
        super(null, XPathParserTreeConstants.JJTINTERSECTEXCEPTEXPR);
    }


    ASTIntersectExceptExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=57fe2367422919138659f8fc5b8715b7 (do not edit this line) */
