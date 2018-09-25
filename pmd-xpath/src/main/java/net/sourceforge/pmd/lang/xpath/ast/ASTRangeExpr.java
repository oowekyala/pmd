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


    ASTRangeExpr(XPathParser p, int id) {
        super(p, id);
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
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=857f43dd522c1b9d0fbd8383846e978c (do not edit this line) */
