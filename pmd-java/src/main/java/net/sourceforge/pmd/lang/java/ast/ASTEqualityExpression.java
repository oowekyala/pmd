/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents an identity test between two values or more values.
 * This has a precedence greater than {@link ASTAndExpression},
 * and lower than {@link ASTInstanceOfExpression} and {@link ASTRelationalExpression}.
 *
 * <pre class="grammar">
 *
 * EqualityExpression ::=  {@linkplain ASTEqualityExpression EqualityExpression} ( "==" | "!=" ) {@linkplain ASTInstanceOfExpression InstanceOfExpression}
 *
 * </pre>
 *
 * <p>Note that the children of this node are not necessarily {@link ASTInstanceOfExpression} or {@link ASTEqualityExpression},
 * rather, they are expressions with an operator precedence greater or equal to those.
 */
public final class ASTEqualityExpression extends AbstractLrBinaryExpr implements ASTExpression {
    ASTEqualityExpression(int id) {
        super(id);
    }

    ASTEqualityExpression(JavaParser p, int id) {
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
