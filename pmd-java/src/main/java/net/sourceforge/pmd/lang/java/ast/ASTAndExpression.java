/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a non-shortcut boolean AND-expression.
 * This has a precedence greater than {@link ASTExclusiveOrExpression},
 * and lower than {@link ASTEqualityExpression}.
 *
 * <pre class="grammar">
 *
 * AndExpression ::= {@link ASTAndExpression AndExpression} "&" {@linkplain ASTEqualityExpression EqualityExpression}
 *
 * </pre>
 */
public final class ASTAndExpression extends AbstractJavaExpr implements ASTBinaryExpression {

    ASTAndExpression(int id) {
        super(id);
    }

    ASTAndExpression(JavaParser p, int id) {
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

    @Override
    public ASTExpression jjtGetChild(int index) {
        return (ASTExpression) super.jjtGetChild(index);
    }

    @Override
    public BinaryOp getOperator() {
        return BinaryOp.AND;
    }
}
