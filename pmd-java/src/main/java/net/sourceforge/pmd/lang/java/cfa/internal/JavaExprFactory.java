/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal;

import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.cfa.exprs.ExprFactoryImpl;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;
import net.sourceforge.pmd.lang.java.ast.UnaryOp;

public class JavaExprFactory extends ExprFactoryImpl<JavaNode> {

    public static final JavaExprFactory INSTANCE = new JavaExprFactory();
    private final ExprVisitor visitor = new ExprVisitor();

    @Override
    public EdgeCondition makeFromAst(JavaNode expression) {
        return (EdgeCondition) expression.jjtAccept(visitor, null);
    }

    /**
     * Maps java expressions to the representation of the framework.
     *
     * TODO there is a big problem with doing that: we blur the line
     *   between what's explicit in source and what is not. Sure it
     *   makes expressions more readable, more easily analysable. But
     *   we need to keep somewhere which expression was written out
     *   and which one is generated.
     */
    private class ExprVisitor extends JavaParserVisitorAdapter {

        @Override
        public Object visit(JavaNode node, Object data) {
            // no recursion
            return EdgeCondition.defaultFactory().makeFromAst(node);
        }

        @Override
        public Object visit(ASTUnaryExpression node, Object data) {
            if (node.getOperator() == UnaryOp.NEGATION) {
                EdgeCondition op = (EdgeCondition) node.getOperand().jjtAccept(this, data);
                return op.negate();
            }
            return super.visit(node, data);
        }

        @Override
        public Object visit(ASTBooleanLiteral node, Object data) {
            return node.isTrue() ? EdgeCondition.TRUE : EdgeCondition.TRUE.negate();
        }

        @Override
        public Object visit(ASTInfixExpression node, Object data) {
            switch (node.getOperator()) {

            case CONDITIONAL_OR:
            case CONDITIONAL_AND:
                EdgeCondition lhs = (EdgeCondition) node.getLeftOperand().jjtAccept(this, data);
                EdgeCondition rhs = (EdgeCondition) node.getRightOperand().jjtAccept(this, data);
                return node.getOperator() == BinaryOp.CONDITIONAL_AND
                       ? lhs.and(rhs) : lhs.or(rhs);
            case EQ:
                return equality(node.getLeftOperand(), node.getRightOperand());
            case NE:
                return equality(node.getLeftOperand(), node.getRightOperand()).negate();
            default:
                return visit((JavaNode) node, data);
            }
        }
    }

}
