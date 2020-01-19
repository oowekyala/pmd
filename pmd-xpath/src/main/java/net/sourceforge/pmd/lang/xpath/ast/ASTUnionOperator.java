/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nonnull;

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
        super(XPathParserImplTreeConstants.JJTUNIONOPERATOR);
    }

    private boolean isShorthand = false;

    @Override
    @Nonnull
    public String getImage() {
        return super.getImage();
    }

    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
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
