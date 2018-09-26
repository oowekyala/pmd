/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Collections;
import java.util.List;


/**
 * Quantified expression.
 *
 * <pre>
 *
 * QuantifiedExpr ::= ("some" | "every") {@linkplain ASTVarBinding VarBinding} "satisfies" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTQuantifiedExpr extends AbstractXPathNode implements ExprSingle, BinderExpr {

    /** Constructor for synthetic node. */
    ASTQuantifiedExpr() {
        super(null, XPathParserTreeConstants.JJTQUANTIFIEDEXPR);
    }

    private boolean isUniversallyQuantified;


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


    /**
     * Returns the variable binding.
     */
    public ASTVarBinding getBinding() {
        return (ASTVarBinding) jjtGetChild(0);
    }


    /**
     * Returns the predicate expression (the one after the "satisfies").
     */
    @Override
    public ExprSingle getBodyExpr() {
        return (ExprSingle) jjtGetChild(1);
    }


    @Override
    public List<ASTVarBinding> getBindings() {
        return Collections.singletonList(getBinding());
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
