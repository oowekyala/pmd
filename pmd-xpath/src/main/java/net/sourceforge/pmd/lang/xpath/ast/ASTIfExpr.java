/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Conditional expression.
 *
 * <pre>
 *
 * IfExpr ::= "if" "(" {@link Expr} ")" "then" {@link ExprSingle} "else" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTIfExpr extends AbstractXPathNode implements ExprSingle {

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
    public ExprSingle getTrueAlternative() {
        return (ExprSingle) getChild(1);
    }


    /**
     * Returns the node that represents the expression that will be evaluated
     * if the guard evaluates to false.
     */
    public ExprSingle getFalseAlternative() {
        return (ExprSingle) getChild(2);
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
