/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * For expression.
 *
 * <pre>
 *
 * ForExpr ::= "for" {@linkplain ASTVarBinding VarBinding} ( "," {@linkplain ASTVarBinding VarBinding} )* "return" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTForExpr extends AbstractXPathNode implements ExprSingle, BinderExpr {

    /** Constructor for synthetic node. */
    public ASTForExpr() {
        super(null, XPathParserTreeConstants.JJTFOREXPR);
    }


    ASTForExpr(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=cc5b747d3c7fa67c70ed3608ab8a905a (do not edit this line) */