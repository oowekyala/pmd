/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a boolean OR-expression. This has a precedence greater than {@link ASTConditionalExpression},
 * and lower than {@link ASTConditionalAndExpression}.
 *
 * <pre class="grammar">
 *
 * ConditionalOrExpression ::= {@link ASTConditionalOrExpression ConditionalOrExpression} "||" {@link ASTConditionalAndExpression ConditionalAndExpression}
 *
 * </pre>
 */
public final class ASTConditionalOrExpression extends AbstractJavaExpr implements ASTExpression, ASTBinaryExpression {

    ASTConditionalOrExpression(int id) {
        super(id);
    }

    ASTConditionalOrExpression(JavaParser p, int id) {
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
        return BinaryOp.CONDITIONAL_OR;
    }
}
