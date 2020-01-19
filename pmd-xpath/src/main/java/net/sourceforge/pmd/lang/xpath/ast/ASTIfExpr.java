/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Conditional expression.
 *
 * <pre>
 *
 * IfExpr ::= "if" "(" {@link Expr} ")" "then" {@link Expr} "else" {@link Expr}
 *
 * </pre>
 */
public final class ASTIfExpr extends AbstractXPathExpr implements Expr {

    /** Constructor for synthetic node. */
    public ASTIfExpr() {
        super(XPathParserImplTreeConstants.JJTIFEXPR);
    }

    ASTIfExpr(int id) {
        this();
    }


    /**
     * Returns the node that represents the guard of this conditional.
     */
    public Expr getCondition() {
        return (Expr) getChild(0);
    }


    /**
     * Returns the node that represents the expression that will be evaluated
     * if the guard evaluates to true.
     */
    public Expr getTrueAlternative() {
        return (Expr) getChild(1);
    }


    /**
     * Returns the node that represents the expression that will be evaluated
     * if the guard evaluates to false.
     */
    public Expr getFalseAlternative() {
        return (Expr) getChild(2);
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
