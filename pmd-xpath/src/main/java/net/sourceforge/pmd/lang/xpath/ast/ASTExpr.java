/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Root class for all expressions. An Expr may have several children,
 * separated by commas. The production ExprSingle is used in this
 * documentation for exprs that cannot have more than one child.
 *
 * <pre>
 *
 * Expr ::= ExprSingle ("," ExprSingle)*
 *
 * ExprSingle ::= {@linkplain ASTForExpr ForExpr}
 *              | {@linkplain ASTLetExpr LetExpr}
 *              | {@linkplain ASTQuantifiedExpr QuantifiedExpr}
 *              | {@linkplain ASTIfExpr IfExpr}
 *              | {@linkplain ASTOrExpr OrExpr}
 *
 * </pre>
 *
 *
 */
public final class ASTExpr extends AbstractXPathNode {


    ASTExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=2e2c123dc1554f24119210ce5dedcec4 (do not edit this line) */
