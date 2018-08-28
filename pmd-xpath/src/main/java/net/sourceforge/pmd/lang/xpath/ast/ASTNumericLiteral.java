/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTNumericLiteral extends AbstractXPathNode implements PrimaryExpr {


    ASTNumericLiteral(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=8602b6687a4251b880ab2cc40720453c (do not edit this line) */
