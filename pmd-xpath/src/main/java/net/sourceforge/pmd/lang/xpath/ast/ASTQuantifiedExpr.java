/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Quantified expression.
 *
 * <pre>
 *
 * QuantifiedExpr ::= ("some" | "every") {@linkplain ASTVarBindingList VarBindingList} "satisfies" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTQuantifiedExpr extends AbstractXPathNode implements ExprSingle {

    private boolean isUniversallyQuantified;


    ASTQuantifiedExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setUniversallyQuantified(boolean b) {
        isUniversallyQuantified = b;
    }


    public boolean isUniversallyQuantified() {
        return isUniversallyQuantified;
    }


    public boolean isExistentiallyQuantified() {
        return !isUniversallyQuantified;
    }


    /**
     * Returns the bindings list of this let  expression.
     */
    public ASTVarBindingList getBindings() {
        return (ASTVarBindingList) jjtGetChild(0);
    }


    /**
     * Returns the tested expression.
     */
    public ExprSingle getTestedExpr() {
        return (ExprSingle) jjtGetChild(1);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=902143576c4105d769904438ccb7bced (do not edit this line) */
