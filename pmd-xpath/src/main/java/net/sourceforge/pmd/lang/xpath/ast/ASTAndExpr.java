/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Boolean AND expression.
 *
 * <pre>
 *
 * AndExpr ::= {@linkplain ASTComparisonExpr ComparisonExpr} ( "and" {@linkplain ASTComparisonExpr ComparisonExpr} )+
 *
 * </pre>
 */
public final class ASTAndExpr extends AbstractXPathNode implements LogicalExpr {

    /** Constructor for synthetic node. */
    public ASTAndExpr() {
        super(XPathParserImplTreeConstants.JJTANDEXPR);
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
/* JavaCC - OriginalChecksum=f0cd041cd1c7403b72ee1922539aeb4a (do not edit this line) */
