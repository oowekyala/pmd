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
public final class ASTCastExpr extends AbstractXPathNode implements Expr {


    ASTCastExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=5ad537d54f890b951d8f2a0ff96687af (do not edit this line) */
