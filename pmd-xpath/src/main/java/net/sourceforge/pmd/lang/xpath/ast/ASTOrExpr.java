/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Boolean OR expression.
 *
 * <pre>
 *
 * OrExpr ::= {@linkplain ASTAndExpr AndExpr} ( "or" {@linkplain ASTAndExpr AndExpr} )+
 *
 * </pre>
 */
public final class ASTOrExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    ASTOrExpr() {
        super(null, XPathParserTreeConstants.JJTOREXPR);
    }


    ASTOrExpr(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=4ac29a32cf9359e1ab1cc65792b744dd (do not edit this line) */
