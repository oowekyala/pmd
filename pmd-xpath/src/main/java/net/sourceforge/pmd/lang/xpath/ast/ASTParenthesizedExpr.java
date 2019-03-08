/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Parenthesized expression. The parentheses bind more tightly than any other expression
 * (this is one of the primary expressions).
 *
 * <pre>
 *
 * ParenthesizedExpr ::= "(" {@link Expr} ")"
 *
 * </pre>
 */
public final class ASTParenthesizedExpr extends AbstractXPathNode implements PrimaryExpr, ParenthesizedNode<Expr> {

    /**
     * Constructor for synthetic node.
     *
     * @param wrapped Node wrapped in the parentheses
     */
    public ASTParenthesizedExpr(Expr wrapped) {
        super(null, XPathParserTreeConstants.JJTPARENTHESIZEDEXPR);
        insertSyntheticChild(wrapped, 0);
    }


    ASTParenthesizedExpr(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the expression wrapped in the parentheses.
     */
    @Override
    public Expr getWrappedNode() {
        return (Expr) jjtGetChild(0);
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
/* JavaCC - OriginalChecksum=90e17dcd950911a5ce0fdff174c3eae1 (do not edit this line) */
