/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

/**
 * Operator occurring in an {@linkplain ASTAdditiveExpr additive expression}.
 *
 * <pre>
 *
 * AdditiveOperator ::= "+" | "-"
 *
 * </pre>
 */
public final class ASTAdditiveOperator extends AbstractXPathNode implements BinaryOperatorNode {

    /** Constructor for synthetic node. */
    public ASTAdditiveOperator() {
        super(null, XPathParserTreeConstants.JJTADDITIVEOPERATOR);
    }


    ASTAdditiveOperator(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the image of the operator, {@literal i.e.} "+" or "-".
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
/* JavaCC - OriginalChecksum=8710d3812b3f1157e5b3459da57560c7 (do not edit this line) */
