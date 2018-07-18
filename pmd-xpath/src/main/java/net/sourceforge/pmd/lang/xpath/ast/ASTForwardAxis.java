/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTForwardAxis extends AbstractXPathNode {
    private String axis;


    ASTForwardAxis(XPathParser p, int id) {
        super(p, id);
    }


    void setAxis(String axis) {
        this.axis = axis;
    }


    public String getAxis() {
        return axis;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=04cbb1ecbe78417f58d0922ba6d038ff (do not edit this line) */
