/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * For expression.
 *
 * <pre>
 *
 * ForExpr ::= "for" {@linkplain ASTVarBinding VarBinding} ( "," {@linkplain ASTVarBinding VarBinding} )* "return" {@link Expr}
 *
 * </pre>
 */
public final class ASTForExpr extends AbstractXPathExpr implements Expr, BinderExpr {

    /** Constructor for synthetic node. */
    public ASTForExpr() {
        super(XPathParserImplTreeConstants.JJTFOREXPR);
    }

    ASTForExpr(int id) {
        this();
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
