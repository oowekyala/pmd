/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents an addition operation on two or more values, or string concatenation.
 * This has a precedence greater than {@link ASTShiftExpression}, and lower
 * than {@link ASTMultiplicativeExpression}.
 *
 *
 * <pre class="grammar">
 *
 * AdditiveExpression ::= {@linkplain ASTAdditiveExpression AdditiveExpression} ( "+" | "-" ) {@linkplain ASTMultiplicativeExpression MultiplicativeExpression}
 *
 * </pre>
 *
 * <p>Note that the children of this node are not necessarily {@link ASTMultiplicativeExpression} or {@link ASTAdditiveExpression},
 * rather, they are expressions with an operator precedence greater or equal to those.
 */
public final class ASTAdditiveExpression extends AbstractLrBinaryExpr {

    ASTAdditiveExpression(int id) {
        super(id);
    }

    ASTAdditiveExpression(JavaParser p, int id) {
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
