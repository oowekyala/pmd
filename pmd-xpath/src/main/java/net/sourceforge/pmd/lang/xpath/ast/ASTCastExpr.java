/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Cast expression.
 *
 * <pre>
 *
 * CastExpr ::= {@linkplain ASTUnaryExpr UnaryExpr} "cast" "as" {@linkplain ASTSingleType SingleType}
 *
 * </pre>
 */
public final class ASTCastExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTCastExpr() {
        super(XPathParserImplTreeConstants.JJTCASTEXPR);
    }


    public ExprSingle getCastedExpr() {
        return (ExprSingle) jjtGetChild(0);
    }


    public ASTSingleType getCastedType() {
        return (ASTSingleType) jjtGetChild(1);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
/* JavaCC - OriginalChecksum=5ad537d54f890b951d8f2a0ff96687af (do not edit this line) */
