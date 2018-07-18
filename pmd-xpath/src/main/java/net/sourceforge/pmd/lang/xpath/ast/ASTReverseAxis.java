/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTReverseAxis extends AbstractXPathNode {
    private String axis;


    ASTReverseAxis(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=c19a5637c34d5b18e3637136cc2c7b3a (do not edit this line) */
