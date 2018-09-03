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
 *
 */
public final class ASTCastableExpr extends AbstractXPathNode implements Expr {


    ASTCastableExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=b535c749ecd93510ad8d8304dbb498e2 (do not edit this line) */
