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
public final class ASTUnionOperator extends AbstractXPathNode {

    private boolean isShorthand = false;


    ASTUnionOperator(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public boolean isShorthand() {
        return isShorthand;
    }


    void setShorthand(boolean shorthand) {
        isShorthand = shorthand;
    }
}
/* JavaCC - OriginalChecksum=e3b812d73cb1799d831dbe10efcac43d (do not edit this line) */
