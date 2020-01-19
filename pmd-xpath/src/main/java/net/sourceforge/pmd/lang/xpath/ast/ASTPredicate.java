/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


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
        super(XPathParserImplTreeConstants.JJTPREDICATE);
    }

    ASTPredicate(int id) {
        this();
    }


    /**
     * Gets the expression wrapped in the predicate.
     */
    public Expr getWrappedExpression() {
        return (Expr) getChild(0);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
