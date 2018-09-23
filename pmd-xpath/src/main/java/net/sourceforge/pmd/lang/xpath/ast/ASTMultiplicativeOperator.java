/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Operator occurring in a {@linkplain ASTMultiplicativeExpr MultiplicativeExpr}.
 *
 * <pre>
 *
 * MultiplicativeOperator ::= "*" | "div" | "idiv" | "mod"
 *
 * </pre>
 */
public final class ASTMultiplicativeOperator extends AbstractXPathNode implements BinaryOperatorNode {


    ASTMultiplicativeOperator(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the image of the operator, {@literal i.e.} "*", "div", "idiv" or "mod".
     */
    @Override
    public String getImage() { // NOPMD
        return super.getImage();
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=2b15cae0a5150eec1469aff6b90bda97 (do not edit this line) */
