/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Boolean OR expression.
 *
 * <pre>
 *
 * OrExpr ::= {@linkplain ASTAndExpr AndExpr} ( "or" {@linkplain ASTAndExpr AndExpr} )+
 *
 * </pre>
 */
public final class ASTOrExpr extends AbstractXPathNode implements LogicalExpr {

    /** Constructor for synthetic node. */
    public ASTOrExpr() {
        super(XPathParserImplTreeConstants.JJTOREXPR);
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
/* JavaCC - OriginalChecksum=4ac29a32cf9359e1ab1cc65792b744dd (do not edit this line) */
