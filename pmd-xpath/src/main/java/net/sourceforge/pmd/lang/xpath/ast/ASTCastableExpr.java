/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Castable expression.
 *
 * <pre>
 *
 * CastableExpr ::= {@linkplain ASTCastExpr CastExpr} "treat" "as" {@linkplain ASTSingleType SingleType}
 *
 * </pre>
 */
public final class ASTCastableExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTCastableExpr() {
        super(null, XPathParserTreeConstants.JJTCASTABLEEXPR);
    }


    ASTCastableExpr(XPathParser p, int id) {
        super(p, id);
    }


    public ExprSingle getTestedExpr() {
        return (ExprSingle) jjtGetChild(0);
    }


    public ASTSingleType getTestedType() {
        return (ASTSingleType) jjtGetChild(1);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }
}
/* JavaCC - OriginalChecksum=b535c749ecd93510ad8d8304dbb498e2 (do not edit this line) */
