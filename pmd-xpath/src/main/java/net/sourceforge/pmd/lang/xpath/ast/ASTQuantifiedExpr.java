/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Quantified expression.
 *
 * <pre>
 *
 * QuantifiedExpr ::= ("some" | "every") {@link ASTVarBinding VarBinding}  ( "," {@link ASTVarBinding VarBinding} )* "satisfies" {@link Expr}
 *
 * </pre>
 */
public final class ASTQuantifiedExpr extends AbstractXPathExpr implements Expr, BinderExpr {

    private boolean isUniversallyQuantified;


    /** Constructor for synthetic node. */
    public ASTQuantifiedExpr() {
        super(XPathParserImplTreeConstants.JJTQUANTIFIEDEXPR);
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
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
