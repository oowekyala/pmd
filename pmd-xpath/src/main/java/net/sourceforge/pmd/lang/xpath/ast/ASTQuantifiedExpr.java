/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Quantified expression.
 *
 * <pre>
 *
 * QuantifiedExpr ::= ("some" | "every") {@linkplain ASTVarBinding VarBinding}  ( "," {@linkplain ASTVarBinding VarBinding} )* "satisfies" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTQuantifiedExpr extends AbstractXPathNode implements ExprSingle, BinderExpr {

    private boolean isUniversallyQuantified;


    /** Constructor for synthetic node. */
    public ASTQuantifiedExpr() {
        super(null, XPathParserTreeConstants.JJTQUANTIFIEDEXPR);
    }


    ASTQuantifiedExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setUniversallyQuantified(boolean b) {
        isUniversallyQuantified = b;
    }


    /**
     * Returns true if this expression asserts that every element
     * of a sequence satisfy the predicate.
     */
    public boolean isUniversallyQuantified() {
        return isUniversallyQuantified;
    }


    /**
     * Returns true if this expression asserts only that some element
     * of a sequence satisfy the predicate.
     */
    public boolean isExistentiallyQuantified() {
        return !isUniversallyQuantified;
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
/* JavaCC - OriginalChecksum=902143576c4105d769904438ccb7bced (do not edit this line) */
