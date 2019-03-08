/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Union operator occurring in an {@linkplain ASTUnionExpr UnionExpr}.
 * <pre>
 *
 * UnionOperator ::= "union" | "|"
 *
 * </pre>
 */
public final class ASTUnionOperator extends AbstractXPathNode implements BinaryOperatorNode {

    /** Constructor for synthetic node. */
    public ASTUnionOperator() {
        super(null, XPathParserTreeConstants.JJTUNIONOPERATOR);
    }

    private boolean isShorthand = false;


    ASTUnionOperator(XPathParser p, int id) {
        super(p, id);
    }

    @Override
    @Nonnull
    public String getImage() {
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
