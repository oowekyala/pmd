/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Additive expression.
 * The XPath specification defines no associativity for these expressions.
 * For parsing simplicity, they're parsed right-associatively. Examples:
 * <ul>
 *     <li>{@code 1 + 2 - 3 * 4 + 5} is parsed as {@code 1 + ((2 - (3 * 4)) + 5)}</li>
 *     <li>{@code 1 + 2 + 5} is parsed as {@code 1 + 2 + 5}, not {@code 1 + (2 + 5)}</li>
 *     <li>{@code 1 - 2 + 5} is parsed as {@code 1 - (2 + 5)}</li>
 * </ul>
 *
 *
 * <pre>
 *
 * AdditiveExpr ::= {@linkplain ASTMultiplicativeExpr MultiplicativeExpr} ( ("+" | "-") {@linkplain ASTMultiplicativeExpr MultiplicativeExpr} )*
 *
 * </pre>
 */
public final class ASTAdditiveExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTAdditiveExpr(ExprSingle a, ExprSingle b, ExprSingle... rest) {
        super(XPathParserImplTreeConstants.JJTADDITIVEEXPR);

        jjtAddChild(a, 0);
        a.jjtSetParent(this);
        jjtAddChild(b, 1);
        b.jjtSetParent(this);
        for (int j = 0; j < rest.length; j++) {
            jjtAddChild(rest[j], j + 2);
            rest[j].jjtSetParent(this);
        }
    }

    /**
     * Returns the image of the operator of this node, ie "+" or "-".
     */
    public String getOperator() {
        return getImage();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=b855e550465145a77a403001a71450fb (do not edit this line) */
