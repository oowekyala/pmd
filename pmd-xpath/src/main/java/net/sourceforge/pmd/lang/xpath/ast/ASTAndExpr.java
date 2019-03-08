/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

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
        super(null, XPathParserTreeConstants.JJTANDEXPR);
    }


    ASTAndExpr(XPathParser p, int id) {
        super(p, id);
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
/* JavaCC - OriginalChecksum=f0cd041cd1c7403b72ee1922539aeb4a (do not edit this line) */
