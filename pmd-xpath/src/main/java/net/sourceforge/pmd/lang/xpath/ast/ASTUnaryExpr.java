/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Unary prefix expression. The sequence of "+" and "-" is concatenated
 * into a string, available from {@link #getOperator()}.
 *
 * <pre>
 *
 * UnaryExpr ::= ("+" | "-")+  {@linkplain ASTMapExpr MapExpr}
 *
 * </pre>
 */
public final class ASTUnaryExpr extends AbstractXPathNode implements ExpressionNode {
    private String operator;


    ASTUnaryExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setOperator(String operator) {
        this.operator = operator;
    }


    public String getOperator() {
        return operator;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=1036550861161e650ddf9b4917bae7c1 (do not edit this line) */
