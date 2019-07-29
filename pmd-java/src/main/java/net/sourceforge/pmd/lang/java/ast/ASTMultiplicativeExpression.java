/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a multiplication, division, or modulo operation on
 * two or more values. This has a precedence greater than {@link ASTAdditiveExpression},
 * and lower than {@linkplain ASTUnaryExpression UnaryExpression}.
 *
 * <pre class="grammar">
 *
 * MultiplicativeExpression ::= {@linkplain ASTMultiplicativeExpression MultiplicativeExpression} ( "*" | "/" | "%" ) {@linkplain ASTUnaryExpression UnaryExpression}
 *
 * </pre>
 *
 * <p>Note that the children of this node are not necessarily {@link ASTUnaryExpression}s or {@link ASTMultiplicativeExpression MultiplicativeExpression},
 * rather, they are expressions with an operator precedence greater or equal to those.
 */
public final class ASTMultiplicativeExpression extends AbstractLrBinaryExpr {


    ASTMultiplicativeExpression(int id) {
        super(id);
    }

    ASTMultiplicativeExpression(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
