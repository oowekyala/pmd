/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Union operator occurring in an {@linkplain ASTUnionExpr UnionExpr}.
 * <pre>
 *
 * UnionOperator ::= "union" | "|"
 *
 * </pre>
 */
public final class ASTUnionOperator extends AbstractXPathNode implements BinaryOperatorNode {

    private boolean isShorthand = false;


    ASTUnionOperator(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Returns true if this operator was written with the shorthand "|" notation
     * instead of the full "union".
     */
    public boolean isShorthand() {
        return isShorthand;
    }


    void setShorthand(boolean shorthand) {
        isShorthand = shorthand;
    }
}
/* JavaCC - OriginalChecksum=e3b812d73cb1799d831dbe10efcac43d (do not edit this line) */
