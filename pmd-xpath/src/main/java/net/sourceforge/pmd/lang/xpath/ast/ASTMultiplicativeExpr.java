/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Multiplicative expression.
 *
 * <pre>
 *
 * MultiplicativeExpr ::= {@linkplain ASTUnionExpr UnionExpr} ({@linkplain ASTMultiplicativeOperator MultiplicativeOperator} {@linkplain ASTUnionExpr UnionExpr})+
 *
 * </pre>
 */
public final class ASTMultiplicativeExpr extends AbstractXPathNode implements ExprSingle {


    ASTMultiplicativeExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=9b618922ba7c08e3304ca129341837fd (do not edit this line) */
