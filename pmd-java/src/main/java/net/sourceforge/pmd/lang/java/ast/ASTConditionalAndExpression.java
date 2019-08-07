/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a boolean AND-expression. This has a precedence greater than {@link ASTConditionalOrExpression},
 * and lower than {@link ASTInclusiveOrExpression}.
 *
 * <pre class="grammar">
 *
 * ConditionalAndExpression ::= {@link ASTAndExpression ConditionalAndExpression} "&amp;&amp;" {@linkplain ASTInclusiveOrExpression InclusiveOrExpression}
 *
 * </pre>
 */
public final class ASTConditionalAndExpression extends AbstractJavaExpr implements ASTExpression, ASTBinaryExpression {

    ASTConditionalAndExpression(int id) {
        super(id);
    }


    ASTConditionalAndExpression(JavaParser p, int id) {
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
        return BinaryOp.CONDITIONAL_AND;
    }
}
