/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTComparisonExpr extends AbstractXPathNode {

    private String operator;


    ASTComparisonExpr(XPathParser p, int id) {
        super(p, id);
    }


    public String getOperator() {
        return operator;
    }


    void setOperator(String s) {
        operator = s;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=6671255ce9211f381c3824ae0513527c (do not edit this line) */
