/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Unary operator.
 */
public enum XpUnaryOp {
    PLUS("+", XPathTokenKinds.PLUS),
    MINUS("-", XPathTokenKinds.MINUS),
    ;

    private final String image;
    private final int tokenKind;

    XpUnaryOp(String image, int tokenKind) {

        this.image = image;
        this.tokenKind = tokenKind;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return image;
    }

    static XpUnaryOp fromTokenKind(int kind) {
        switch (kind) {

        case XPathTokenKinds.PLUS:
            return PLUS;
        case XPathTokenKinds.MINUS:
            return MINUS;

        default:
            throw new IllegalArgumentException(XPathTokenKinds.describe(kind) + " is no valid unary operator kind");
        }
    }
}
