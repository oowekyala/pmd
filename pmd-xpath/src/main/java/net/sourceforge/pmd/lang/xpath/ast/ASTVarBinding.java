/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTVarBinding extends AbstractXPathNode {


    private String varName;


    ASTVarBinding(XPathParser p, int id) {
        super(p, id);
    }


    void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public String getVarName() {
        return varName;
    }
}
/* JavaCC - OriginalChecksum=0801906c16745525f42098e542a6ff4e (do not edit this line) */
