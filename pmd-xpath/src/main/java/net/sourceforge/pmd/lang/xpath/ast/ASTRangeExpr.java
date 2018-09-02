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
public final class ASTRangeExpr extends AbstractXPathNode implements ExpressionNode {


    ASTRangeExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=857f43dd522c1b9d0fbd8383846e978c (do not edit this line) */
