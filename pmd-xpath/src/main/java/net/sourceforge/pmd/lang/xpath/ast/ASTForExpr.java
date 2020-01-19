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
        super(XPathParserImplTreeConstants.JJTFOREXPR);
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
/* JavaCC - OriginalChecksum=cc5b747d3c7fa67c70ed3608ab8a905a (do not edit this line) */
