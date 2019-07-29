/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents a non-shortcut boolean OR-expression. This has a precedence
 * greater than {@link ASTConditionalAndExpression}, and lower than
 * {@link ASTExclusiveOrExpression}.
 *
 * <p>Note that the children of this node are not necessarily {@link ASTExclusiveOrExpression},
 * rather, they are expressions with an operator precedence greater or equal to ExclusiveOrExpression.
 *
 *
 * <pre class="grammar">
 *
 * InclusiveOrExpression ::= {@link ASTInclusiveOrExpression InclusiveOrExpression} "|" {@linkplain ASTExclusiveOrExpression ExclusiveOrExpression}
 *
 * </pre>
 */
public final class ASTInclusiveOrExpression extends AbstractJavaExpr implements ASTBinaryExpression {

    ASTInclusiveOrExpression(int id) {
        super(id);
    }


    ASTInclusiveOrExpression(JavaParser p, int id) {
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
        return BinaryOp.OR;
    }
}
