/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

// @formatter:off
/**
 * Represents a binary infix expression. {@linkplain ASTAssignmentExpression Assignment expressions}
 * are not considered binary expressions, because they're right-associative.
 *
 * <pre class="grammar">
 *
 * BinaryExpression ::= {@link ASTConditionalExpression ConditionalExpression}
 *                    | {@link ASTConditionalOrExpression ConditionalOrExpression}
 *                    | {@link ASTConditionalAndExpression ConditionalAndExpression}
 *                    | {@link ASTInclusiveOrExpression InclusiveOrExpression}
 *                    | {@link ASTExclusiveOrExpression ExclusiveOrExpression}
 *                    | {@link ASTAndExpression AndExpression}
 *                    | {@link ASTEqualityExpression AndExpression}
 *                    | {@link ASTRelationalExpression RelationalExpression}
 *                    | {@link ASTShiftExpression ShiftExpression}
 *                    | {@link ASTAdditiveExpression AdditiveExpression}
 *                    | {@link ASTMultiplicativeExpression MultiplicativeExpression}
 * </pre>

 *
 *
 * <p>Binary expressions are all left-associative, and are parsed left-recursively.
 * For example, the expression {@code 1 * 2 * 3 % 4} parses as the following tree:
 *
 * <figure>
 *     <img src="doc-files/binaryExpr_70x.svg" />
 * </figure>
 *
 * <p>In PMD 6.0.x, it would have parsed into the tree:
 *
 * <figure>
 *     <img src="doc-files/binaryExpr_60x.svg" />
 * </figure>
 *
 */
// TODO instanceOf also is also kind of binary..
// @formatter:on
public interface ASTBinaryExpression extends ASTExpression, JSingleChildNode<ASTExpression>, LeftRecursiveNode {


    /** Returns the operator. */
    BinaryOp getOperator();


    /** Returns the left-hand-side operand. */
    default ASTExpression getLhs() {
        return jjtGetChild(0);
    }


    /** Returns the right-hand-side operand. */
    default ASTExpression getRhs() {
        return jjtGetChild(1);
    }


    @Override
    default NodeMetaModel<? extends JavaNode> metaModel() {
        return new NodeMetaModel<>(getClass(), 2);
    }
}
