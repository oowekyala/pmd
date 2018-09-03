/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Additive expression.
 *
 * <pre>
 *
 * AdditiveExpr ::= {@linkplain ASTMultiplicativeExpr MultiplicativeExpr} ( {@linkplain ASTAdditiveOperator AdditiveOperator} {@linkplain ASTMultiplicativeExpr MultiplicativeExpr} )*
 *
 * </pre>
 */
public final class ASTAdditiveExpr extends AbstractXPathNode implements Expr {


    ASTAdditiveExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=b855e550465145a77a403001a71450fb (do not edit this line) */
