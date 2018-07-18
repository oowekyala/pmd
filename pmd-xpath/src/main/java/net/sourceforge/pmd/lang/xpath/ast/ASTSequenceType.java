/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTSequenceType extends AbstractXPathNode {

    private Cardinality cardinality;


    ASTSequenceType(XPathParser p, int id) {
        super(p, id);
    }


    void setCardinality(Cardinality card) {
        this.cardinality = card;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=d53954607fe0124e2dded5a96f8b404b (do not edit this line) */
