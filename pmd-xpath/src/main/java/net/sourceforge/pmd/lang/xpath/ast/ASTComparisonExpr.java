/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Comparison expression.
 *
 * <pre>
 *
 * ComparisonExpr ::=  {@linkplain ASTStringConcatExpr StringConcatExpr} ComparisonOperator {@linkplain ASTStringConcatExpr StringConcatExpr}
 *
 * (: Not a node :)
 * ComparisonOperator ::= "=" | "!=" | "<" | "<=" | ">" | ">=" | ">>" | "<<"
 *                      | "ne" | "eq" | "lt" | "le" | "gt" | "ge"
 *
 * </pre>
 */
public final class ASTComparisonExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    ASTComparisonExpr() {
        super(null, XPathParserTreeConstants.JJTCOMPARISONEXPR);
    }

    private String operator;


    ASTComparisonExpr(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the image of the operator.
     */
    public String getOperator() {
        return operator;
    }


    /**
     * Gets the left hand side of the comparison.
     */
    public Expr getLhs() {
        return (Expr) jjtGetChild(0);
    }


    /**
     * Gets the right hand side of the comparison.
     */
    public Expr getRhs() {
        return (Expr) jjtGetChild(1);
    }


    void setOperator(String s) {
        operator = s;
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
/* JavaCC - OriginalChecksum=6671255ce9211f381c3824ae0513527c (do not edit this line) */
