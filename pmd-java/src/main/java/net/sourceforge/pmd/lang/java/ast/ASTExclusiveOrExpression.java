/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents an exclusive OR-expression. Depending on the type of the
 * operands, the operator is either bitwise (numeric) or logical (boolean).
 * This has a precedence greater than {@link ASTInclusiveOrExpression},
 * and lower than {@link ASTAndExpression}.
 *
 * <pre class="grammar">
 *
 * ExclusiveOrExpression ::= {@link ASTExclusiveOrExpression ExclusiveOrExpression} "^" {@linkplain ASTAndExpression AndExpression}
 *
 * </pre>
 */
public final class ASTExclusiveOrExpression extends AbstractJavaExpr implements ASTBinaryExpression {

    ASTExclusiveOrExpression(int id) {
        super(id);
    }

    ASTExclusiveOrExpression(JavaParser p, int id) {
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
        return BinaryOp.XOR;
    }
}
