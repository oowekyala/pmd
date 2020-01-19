/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * A let-expression binds some names to expressions and allow these variables
 * to be referenced in its body.
 *
 * <pre>
 *
 * LetExpr ::= "let" {@linkplain ASTVarBinding VarBinding} ( "," {@linkplain ASTVarBinding VarBinding} )* "return" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTLetExpr extends AbstractXPathNode implements ExprSingle, BinderExpr {

    /** Constructor for synthetic node. */
    public ASTLetExpr() {
        super(XPathParserImplTreeConstants.JJTLETEXPR);
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
/* JavaCC - OriginalChecksum=78269336e6bae8882c7fd308eae64438 (do not edit this line) */
