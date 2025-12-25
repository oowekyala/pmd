/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.FALSE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.TRUE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.BooleanValueAnalysis.BooleanModel.UNKNOWN;

import java.util.EnumSet;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTNullLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.ast.UnaryOp;
import net.sourceforge.pmd.lang.java.types.JPrimitiveType.PrimitiveTypeKind;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.util.AssertionUtil;
import net.sourceforge.pmd.util.DataMap;

public class BooleanValueAnalysis extends ValueAnalysis<BooleanModel> {

    public static final EnumSet<BinaryOp> BOOLEAN_OPS =
        EnumSet.of(BinaryOp.OR, BinaryOp.CONDITIONAL_OR, BinaryOp.AND, BinaryOp.CONDITIONAL_AND, BinaryOp.XOR);

    BooleanValueAnalysis(DataMap.SimpleDataKey<BooleanModel> cacheKey) {
        super(cacheKey);
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
    protected @NonNull BooleanModel createFieldDefaultModel(ASTVariableId varId) {
        return varId.getTypeMirror().isPrimitive(PrimitiveTypeKind.BOOLEAN) ? FALSE : UNKNOWN;
    }

    @Override
    protected BooleanModel createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr, DataflowScope scope) {
        // todo look at the iterable for annotations
        return createModelBasedOnType(varType);
    }

    @Override
    protected ValueAnalysis<BooleanModel>.CreateExprModelVisitor createModelForSimpleExprVisitor() {
        return new BooleanVisitor();
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

        @Override
        public boolean isTop() {
            return this == UNKNOWN;
        }

        @Override
        public boolean isBottom() {
            return this == EMPTY;
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

    class BooleanVisitor extends CreateExprModelVisitor {


        @Override
        public BooleanModel visit(ASTInfixExpression node, DataflowScope scope) {
            ASTExpression lhs = node.getLeftOperand();
            ASTExpression rhs = node.getRightOperand();
            BinaryOp operator = node.getOperator();

            if (BOOLEAN_OPS.contains(operator)) {
                // These are operations that require boolean operands,
                // therefore we need to get models for the branches.

                BooleanModel left = getModel(lhs, scope);
                BooleanModel right = getModel(rhs, scope);
                switch (operator) {
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
                default:
                    throw AssertionUtil.shouldNotReachHere("exhaustive switch");
                }
            }

            // The rest of the operators use different analyses.

            switch (operator) {
            case EQ:
            case NE:
                if (lhs instanceof ASTNullLiteral || rhs instanceof ASTNullLiteral) {
                    // This is a null check. Maybe the nullability analysis
                    // knows something about this.
                    ASTExpression nullChecked = lhs instanceof ASTNullLiteral ? rhs : lhs;
                    NullabilityAnalysis.Nullability nullability = getModel(nullChecked, scope, NullabilityAnalysis.class);
                    BooleanModel result = UNKNOWN;
                    if (nullability == NullabilityAnalysis.Nullability.NULL) {
                        result = TRUE;
                    } else if (nullability == NullabilityAnalysis.Nullability.NONNULL) {
                        result = FALSE;
                    }
                    return operator == BinaryOp.NE ? result.negate() : result;
                }
                return UNKNOWN;
            }
            return UNKNOWN;
        }


        @Override
        public BooleanModel visit(ASTUnaryExpression node, DataflowScope scope) {
            BooleanModel operandModel = getModel(node.getOperand(), scope);
            if (node.getOperator() == UnaryOp.NEGATION) {
                return operandModel.negate();
            }
            return UNKNOWN;
        }

    /*
        Primaries
     */

        @Override
        public BooleanModel visit(ASTMethodCall node, DataflowScope scope) {
            return unknown(); // todo
        }

        @Override
        public BooleanModel visit(ASTBooleanLiteral node, DataflowScope scope) {
            return node.isTrue() ? TRUE : FALSE;
        }
    }
}
