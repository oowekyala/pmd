/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

/**
 * A predicate occurring in a {@linkplain StepExpr StepExpr}.
 *
 * <pre>
 *
 * Predicate ::= "[" {@link Expr} "]"
 *
 * </pre>
 */
public final class ASTPredicate extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTPredicate() {
        super(null, XPathParserTreeConstants.JJTPREDICATE);
    }


    ASTPredicate(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the expression wrapped in the predicate.
     */
    public Expr getWrappedExpression() {
        return (Expr) jjtGetChild(0);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }
}
/* JavaCC - OriginalChecksum=da66ad6f42ac28b3cf50f8457dd9033a (do not edit this line) */
