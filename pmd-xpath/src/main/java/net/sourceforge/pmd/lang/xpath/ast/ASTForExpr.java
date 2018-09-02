/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * For expression.
 *
 * <pre>
 *
 * ForExpr ::= "for" {@linkplain ASTVarBindingList VarBindingList} "return" {@linkplain ASTExpr ExprSingle}
 *
 * </pre>
 */
public final class ASTForExpr extends AbstractXPathNode implements ExpressionNode {


    ASTForExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=cc5b747d3c7fa67c70ed3608ab8a905a (do not edit this line) */
