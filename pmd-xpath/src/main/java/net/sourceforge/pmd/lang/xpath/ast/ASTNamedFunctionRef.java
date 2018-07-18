/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTNamedFunctionRef extends AbstractXPathNode {

    private int arity = 0;


    ASTNamedFunctionRef(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public int getArity() {
        return arity;
    }


    void setArity(int arity) {
        this.arity = arity;
    }
}
/* JavaCC - OriginalChecksum=ec8948cb98fce8414cc0bdb9249c4c98 (do not edit this line) */
