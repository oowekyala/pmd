/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Operator occurring in a {@linkplain ASTIntersectExceptExpr IntersectExceptExpr}.
 *
 * TODO would left recursive parsing be better?
 *
 * <pre>
 *
 * IntersectExceptOperator ::= "intersect" | "except"
 *
 * </pre>
 */
public final class ASTIntersectExceptOperator extends AbstractXPathNode implements BinaryOperatorNode {


    ASTIntersectExceptOperator(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the image of the operator as it appeared in the source,
     * {@literal i.e.} "intersect" or "except".
     */
    @Override
    public String getImage() { // NOPMD
        return super.getImage();
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=36d721d16fef866a8ba7d30d87222f71 (do not edit this line) */
