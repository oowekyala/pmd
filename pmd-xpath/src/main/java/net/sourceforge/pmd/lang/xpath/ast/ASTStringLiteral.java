/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTStringLiteral extends AbstractXPathNode implements PrimaryExpr {


    ASTStringLiteral(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public String getImage() {
        return jjtGetFirstToken().getImage();
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=98f7aaa4be4b56badb9f2abeb228cb00 (do not edit this line) */
