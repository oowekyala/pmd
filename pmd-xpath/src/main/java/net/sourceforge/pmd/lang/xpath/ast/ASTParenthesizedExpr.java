/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

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
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=90e17dcd950911a5ce0fdff174c3eae1 (do not edit this line) */
