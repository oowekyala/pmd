/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTPathOperator extends AbstractXPathNode {

    private boolean isChildAxis = false;


    ASTPathOperator(XPathParser p, int id) {
        super(p, id);
    }


    public boolean isChildAxis() {
        return isChildAxis;
    }


    void setChildAxis(boolean childAxis) {
        isChildAxis = childAxis;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=8710d3812b3f1157e5b3459da57560c7 (do not edit this line) */
