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
        super(XPathParserImplTreeConstants.JJTINTERSECTEXCEPTEXPR);
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
/* JavaCC - OriginalChecksum=57fe2367422919138659f8fc5b8715b7 (do not edit this line) */
