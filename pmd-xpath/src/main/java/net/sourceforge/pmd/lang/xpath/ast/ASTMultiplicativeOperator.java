/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

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

    /** Constructor for synthetic node. */
    public ASTMultiplicativeOperator() {
        super(null, XPathParserTreeConstants.JJTMULTIPLICATIVEOPERATOR);
    }


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
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=2b15cae0a5150eec1469aff6b90bda97 (do not edit this line) */
