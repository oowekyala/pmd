/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Multiplicative expression.
 *
 * <pre>
 *
 * MultiplicativeExpr     ::= {@linkplain ASTUnionExpr UnionExpr} (MultiplicativeOperator {@linkplain ASTUnionExpr UnionExpr})+
 *
 * MultiplicativeOperator ::= "*" | "div" | "idiv" | "mod"
 *
 * </pre>
 */
public final class ASTMultiplicativeExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTMultiplicativeExpr() {
        super(XPathParserImplTreeConstants.JJTMULTIPLICATIVEEXPR);
    }


    /**
     * Returns the image of the operator of this node, ie "+" or "-".
     */
    public String getOperator() {
        return getImage();
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
/* JavaCC - OriginalChecksum=9b618922ba7c08e3304ca129341837fd (do not edit this line) */
