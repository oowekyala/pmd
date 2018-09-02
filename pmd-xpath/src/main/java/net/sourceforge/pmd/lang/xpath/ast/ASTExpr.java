/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * Root class for all expressions. An Expr may have several children,
 * separated by commas. The production ExprSingle is used in this
 * documentation for expressions that cannot have more than one child.
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
public final class ASTExpr extends AbstractXPathNode implements Iterable<ExpressionNode> {


    ASTExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<ExpressionNode> iterator() {
        return new NodeChildrenIterator<>(this, ExpressionNode.class);
    }
}
/* JavaCC - OriginalChecksum=2e2c123dc1554f24119210ce5dedcec4 (do not edit this line) */
