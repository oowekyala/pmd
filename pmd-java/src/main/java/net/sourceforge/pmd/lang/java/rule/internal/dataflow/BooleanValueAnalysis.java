/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.FALSE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.TRUE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.UNKNOWN;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTCastExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.ast.UnaryOp;
import net.sourceforge.pmd.lang.java.types.JPrimitiveType.PrimitiveTypeKind;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

public class BooleanValueAnalysis extends DfAnalysis<BooleanModel> {

    public BooleanModel getBooleanModel(ASTExpression e) {
        return getModel(e);
    }

    @Override
    protected @NonNull BooleanModel unknown() {
        return UNKNOWN;
    }

    @Override
    protected @NonNull BooleanModel empty() {
        return EMPTY;
    }

    @Override
    public @Nullable BooleanModel createModel(@NonNull ASTExpression expr) {
        return expr.acceptVisitor(BooleanVisitor.INSTANCE, this);
    }

    @Override
    protected @Nullable BooleanModel createFieldDefaultModel(ASTVariableId varId) {
        return varId.getTypeMirror().isPrimitive(PrimitiveTypeKind.BOOLEAN) ? FALSE : null;
    }

    @Override
    protected @NonNull BooleanModel createModelBasedOnType(JTypeMirror type) {
        return UNKNOWN; // todo use annotations
    }

    @Override
    protected BooleanModel createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr) {
        // todo look at the iterable for annotations
        return createModelBasedOnType(varType);
    }

    @Override
    protected BooleanModel computeModelOfReachingDefinitions(ASTAssignableExpr.ASTNamedReferenceExpr node) {
        return super.computeModelOfReachingDefinitions(node);
    }

    /**
     * Model for a boolean value.
     */
    public enum BooleanModel implements ValueModel<BooleanModel> {
        EMPTY,
        FALSE,
        TRUE,
        UNKNOWN;

        @Override
        public BooleanModel join(BooleanModel other) {
            if (this == FALSE && other == TRUE
                || this == TRUE && other == FALSE) {
                return UNKNOWN;
            }
            return this.compareTo(other) < 0 ? other : this;
        }

        public BooleanModel negate() {
            switch (this) {
            case FALSE:
                return TRUE;
            case TRUE:
                return FALSE;
            default:
                return this;
            }
        }
    }

    static class BooleanVisitor extends JavaVisitorBase<BooleanValueAnalysis, BooleanModel> {

        static final BooleanVisitor INSTANCE = new BooleanVisitor();


        @Override
        public BooleanModel visitExpression(ASTExpression node, BooleanValueAnalysis data) {
            return null;
        }

        @Override
        public BooleanModel visit(ASTAssignmentExpression node, BooleanValueAnalysis data) {
            return data.getBooleanModel(node.getRightOperand());
        }

        @Override
        public BooleanModel visit(ASTConditionalExpression node, BooleanValueAnalysis data) {
            BooleanModel condition = data.getBooleanModel(node.getCondition());
            if (condition == TRUE) {
                return data.getBooleanModel(node.getThenBranch());
            } else if (condition == FALSE) {
                return data.getBooleanModel(node.getElseBranch());
            }
            BooleanModel thenBool = data.getBooleanModel(node.getThenBranch());
            BooleanModel elseBool = data.getBooleanModel(node.getElseBranch());
            return thenBool.join(elseBool);
        }


        @Override
        public BooleanModel visit(ASTInfixExpression node, BooleanValueAnalysis data) {
            // todo this is actually not flexible enough I think.
            //  Because conditional expressions have their own control flow,
            //  in a || b, if you're executing b then you can assume !a.
            //

            BooleanModel left = data.getBooleanModel(node.getLeftOperand());
            BooleanModel right = data.getBooleanModel(node.getRightOperand());

            switch (node.getOperator()) {
            case OR:
            case CONDITIONAL_OR:
                if (left == TRUE || right == TRUE) {
                    return TRUE;
                } else if (left == FALSE && right == FALSE) {
                    return FALSE;
                }
                return UNKNOWN;
            case CONDITIONAL_AND:
            case AND:
                if (left == FALSE || right == FALSE) {
                    return FALSE;
                } else if (left == TRUE && right == TRUE) {
                    return TRUE;
                }
                return UNKNOWN;

            case XOR:
                if (left == UNKNOWN || right == UNKNOWN
                    || left == EMPTY || right == EMPTY) {
                    return UNKNOWN;
                }
                return left != right ? TRUE : FALSE;
            case EQ:
            case NE:
                // todo
                return UNKNOWN;
            }
            return UNKNOWN;
        }


        @Override
        public BooleanModel visit(ASTUnaryExpression node, BooleanValueAnalysis data) {
            BooleanModel operandModel = data.getBooleanModel(node.getOperand());
            if (node.getOperator() == UnaryOp.NEGATION) {
                return operandModel.negate();
            }
            return null;
        }

        @Override
        public BooleanModel visit(ASTCastExpression node, BooleanValueAnalysis data) {
            return data.getBooleanModel(node.getOperand());
        }


        @Override
        public BooleanModel visit(ASTSwitchExpression node, BooleanValueAnalysis data) {
            return UNKNOWN; // TODO
        }


    /*
        Primaries
     */

        @Override
        public BooleanModel visit(ASTMethodCall node, BooleanValueAnalysis data) {
            return visitPrimaryExpr(node, data);
        }

        @Override
        public BooleanModel visit(ASTArrayAccess node, BooleanValueAnalysis data) {
            // todo. actually we need to be able to query another analysis from here,
            //  to get the corresponding array model
            return UNKNOWN;
        }

        @Override
        public BooleanModel visit(ASTVariableAccess node, BooleanValueAnalysis data) {
            return data.computeModelOfReachingDefinitions(node); // todo same here actually
        }

        @Override
        public BooleanModel visit(ASTFieldAccess node, BooleanValueAnalysis data) {
            return data.computeModelOfReachingDefinitions(node);
        }

        @Override
        public BooleanModel visit(ASTBooleanLiteral node, BooleanValueAnalysis data) {
            return node.isTrue() ? TRUE : FALSE;
        }
    }
}
