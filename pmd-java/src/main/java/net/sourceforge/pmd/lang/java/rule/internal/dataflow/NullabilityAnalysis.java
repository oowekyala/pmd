/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NONNULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULLABLE;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.UNKNOWN;

import java.util.EnumSet;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTArrayAllocation;
import net.sourceforge.pmd.lang.java.ast.ASTClassLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodReference;
import net.sourceforge.pmd.lang.java.ast.ASTNullLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTSuperExpression;
import net.sourceforge.pmd.lang.java.ast.ASTThisExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.symbols.SymbolicValue;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.OverloadSelectionResult;
import net.sourceforge.pmd.util.AssertionUtil;
import net.sourceforge.pmd.util.DataMap;
import net.sourceforge.pmd.util.OptionalBool;

/**
 * A value analysis that tracks nullability of values.
 * This understands nullability annotations (TODO make configurable).
 */
public class NullabilityAnalysis extends ValueAnalysis<Nullability> {

    NullabilityAnalysis(DataMap.SimpleDataKey<Nullability> cacheKey) {
        super(cacheKey);
    }

    @Override
    protected @NonNull Nullability unknown() {
        return UNKNOWN;
    }

    @Override
    protected @NonNull Nullability empty() {
        return EMPTY;
    }

    @Override
    protected @NonNull Nullability createFieldDefaultModel(ASTVariableId varId) {
        return varId.getTypeNode() instanceof ASTPrimitiveType ? UNKNOWN : NULL;
    }

    @Override
    protected @NonNull Nullability createModelBasedOnType(JTypeMirror type) {
        if (type.isPrimitive()) {
            return NONNULL;
        }
        // This is written so that
        // No annotations -> UNKNOWN
        // Contradictory annotations -> UNKNOWN
        Nullability result = EMPTY;
        for (SymbolicValue.SymAnnot annot : type.getTypeAnnotations()) {
            if (isNullableAnnotation(annot)) {
                result = result.join(NULLABLE);
            } else if (isNonNullAnnotation(annot)) {
                result = result.join(NONNULL);
            }
        }
        return result == EMPTY ? UNKNOWN : result;
    }

    private static boolean isNonNullAnnotation(SymbolicValue.SymAnnot annot) {
        return annot.getSimpleName().equals("NonNull");
    }

    private static boolean isNullableAnnotation(SymbolicValue.SymAnnot annot) {
        return annot.getSimpleName().equals("Nullable");
    }

    @Override
    protected Nullability createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr, DataflowScope scope) {
        // todo look at the iterable for annotations
        return createModelBasedOnType(varType);
    }

    @Override
    protected ValueAnalysis<Nullability>.CreateExprModelVisitor createModelForSimpleExprVisitor() {
        return new NullabilityVisitor();
    }

    static final class Assumptions {
        static final Assumptions NO_ASSUMPTIONS = new Assumptions(s -> {}, s -> {});

        private final Consumer<DataflowScope> whenTrue;
        private final Consumer<DataflowScope> whenFalse;

        public Assumptions(Consumer<DataflowScope> whenTrue, Consumer<DataflowScope> whenFalse) {
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
        }

        public Assumptions negate() {
            return new Assumptions(whenFalse, whenTrue);
        }
    }

    /**
     * Given a boolean expression, what does its being true or
     * false imply about the values it uses? The boolean expr
     * will never be a ternary, an {@code AND}, {@code OR}, {@code XOR}
     * or conditional {@code AND} or {@code OR} expression.
     *
     * @param boolExpr A boolean expression atom
     * @return assumptions
     */
    Assumptions backwardsBoolAnalysis(ASTExpression boolExpr, DataflowScope baseScope) {
        if (JavaAstUtils.isInfixExprWithOperator(boolExpr, EnumSet.of(BinaryOp.EQ, BinaryOp.NE))) {
            ASTInfixExpression infix = (ASTInfixExpression) boolExpr;
            ASTExpression nullChecked;
            if (infix.getLeftOperand() instanceof ASTNullLiteral) {
                nullChecked = infix.getRightOperand();
            } else if (infix.getRightOperand() instanceof ASTNullLiteral) {
                nullChecked = infix.getLeftOperand();
            } else {
                return Assumptions.NO_ASSUMPTIONS;
            }

            Assumptions eqAssumptions = new Assumptions(
                scope -> scope.setModel(nullChecked, NULL, this),
                scope -> scope.setModel(nullChecked, NONNULL, this)
            );
            return infix.getOperator() == BinaryOp.NE ? eqAssumptions.negate() : eqAssumptions;
        }
        return Assumptions.NO_ASSUMPTIONS;
    }

    /**
     * Model for any reference value, that just supports checking the
     * nullability state.
     */
    public enum Nullability implements ValueModel<Nullability> {
        EMPTY,
        NULL,
        NONNULL,
        NULLABLE,
        UNKNOWN;

        @Override
        public Nullability join(Nullability other) {
            if (this == NULL && other == NONNULL
                || this == NONNULL && other == NULL) {
                return NULLABLE;
            }
            return this.compareTo(other) < 0 ? other : this;
        }

        /**
         * Returns whether null is a possible value of a variable in
         * this state.
         */
        public OptionalBool mayBeNull() {
            switch (this) {
            case EMPTY:
            case NONNULL:
                return OptionalBool.NO;
            case NULL:
            case NULLABLE:
                return OptionalBool.YES;
            case UNKNOWN:
                return OptionalBool.UNKNOWN;
            default:
                throw AssertionUtil.exhaustiveSwitch();
            }
        }

        @Override
        public boolean isTop() {
            return this == UNKNOWN;
        }

        @Override
        public boolean isBottom() {
            return this == EMPTY;
        }
    }

    class NullabilityVisitor extends CreateExprModelVisitor {

        @Override
        public Nullability visit(ASTLambdaExpression node, DataflowScope data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTInfixExpression node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTUnaryExpression node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTMethodCall node, DataflowScope scope) {
            OverloadSelectionResult info = node.getOverloadSelectionInfo();
            if (info.isFailed()) {
                return UNKNOWN;
            }
            return createModelBasedOnType(info.getMethodType().getReturnType());
        }

        @Override
        public Nullability visit(ASTConstructorCall node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTArrayAllocation node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTArrayAccess node, DataflowScope scope) {
            // todo. actually we need to be able to query another analysis from here,
            //  to get the corresponding array model
            return UNKNOWN;
        }

        @Override
        public Nullability visit(ASTMethodReference node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTThisExpression node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTSuperExpression node, DataflowScope scope) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTClassLiteral node, DataflowScope scope) {
            return NONNULL;
        }

        public Nullability visitLiteral(ASTLiteral node, DataflowScope scope) {
            // Other literals delegate here
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTNullLiteral node, DataflowScope scope) {
            return NULL;
        }
    }
}
