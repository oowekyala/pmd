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
public final class ASTAdditiveExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTAdditiveExpr() {
        super(null, XPathParserTreeConstants.JJTADDITIVEEXPR);
    }


    ASTAdditiveExpr(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=b855e550465145a77a403001a71450fb (do not edit this line) */
