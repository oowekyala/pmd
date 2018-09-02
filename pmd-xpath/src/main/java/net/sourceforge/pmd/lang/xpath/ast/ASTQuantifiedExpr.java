/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTQuantifiedExpr extends AbstractXPathNode implements ExpressionNode {

    private boolean isUniversallyQuantified;


    ASTQuantifiedExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setUniversallyQuantified(boolean b) {
        isUniversallyQuantified = b;
    }


    public boolean isUniversallyQuantified() {
        return isUniversallyQuantified;
    }


    public boolean isExistentiallyQuantified() {
        return !isUniversallyQuantified;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=902143576c4105d769904438ccb7bced (do not edit this line) */
