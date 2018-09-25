/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Union expression.
 *
 * <pre>
 *
 * UnionExpr ::= {@linkplain ASTIntersectExceptExpr IntersectExceptExpr} ( {@linkplain ASTUnionOperator UnionOperator} {@linkplain ASTIntersectExceptExpr IntersectExceptExpr} )+
 *
 * </pre>
 */
public final class ASTUnionExpr extends AbstractXPathNode implements ExprSingle {


    ASTUnionExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=f4b5692c47307c942396bd9a53195c5e (do not edit this line) */
