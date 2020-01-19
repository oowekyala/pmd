/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Range expression.
 *
 * <pre>
 *
 * RangeExpr ::= {@linkplain ASTAdditiveExpr AdditiveExpr} "to" {@linkplain ASTAdditiveExpr AdditiveExpr}
 *
 * </pre>
 */
public final class ASTRangeExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTRangeExpr() {
        super(XPathParserImplTreeConstants.JJTRANGEEXPR);
    }


    /**
     * Gets the lower bound of the range.
     */
    public Expr getLowerBound() {
        return (Expr) jjtGetChild(0);
    }


    /**
     * Gets the upper bound of the range.
     */
    public Expr getUpperBound() {
        return (Expr) jjtGetChild(1);
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
/* JavaCC - OriginalChecksum=857f43dd522c1b9d0fbd8383846e978c (do not edit this line) */
