/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTNumericLiteral extends AbstractXPathNode implements PrimaryExpr {

    private double value;
    private boolean isIntegerLiteral;
    private boolean isDecimalLiteral;
    private boolean isDoubleLiteral;


    ASTNumericLiteral(XPathParser p, int id) {
        super(p, id);
    }


    public boolean isIntegerLiteral() {
        return isIntegerLiteral;
    }


    void setIntegerLiteral() {
        isIntegerLiteral = true;
    }


    public boolean isDecimalLiteral() {
        return isDecimalLiteral;
    }


    void setDecimalLiteral() {
        isDecimalLiteral = true;
    }


    public boolean isDoubleLiteral() {
        return isDoubleLiteral;
    }


    void setDoubleLiteral() {
        isDoubleLiteral = true;
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
