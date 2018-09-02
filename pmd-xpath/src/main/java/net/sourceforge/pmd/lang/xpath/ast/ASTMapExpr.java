/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Map expression.
 *
 * <pre>
 *
 * MapExpr ::= {@linkplain ASTPathExpr PathExpr} ( "!" {@linkplain ASTPathExpr PathExpr} )+
 *
 * </pre>
 */
public final class ASTMapExpr extends AbstractXPathNode implements ExpressionNode {


    ASTMapExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=4cbaf35312e52a85f9f37924cf27e023 (do not edit this line) */
