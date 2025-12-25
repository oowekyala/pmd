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

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTNullLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.symbols.SymbolicValue;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.OverloadSelectionResult;
import net.sourceforge.pmd.util.DataMap;

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

    @Override
    protected Assumptions backwardsBoolAnalysis(ASTExpression boolExpr, DataflowScope baseScope) {
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

            // todo if you write
            //     if (a == null || a != null) { }
            //  then we need to merge both states.
            //  In the then branch, a should be NULLABLE.
            //  And if you write
            //     if (a == null || b != null)
            //  In the then branch, a should be NULL.join(before)
            //
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
        public Nullability visitExpression(ASTExpression node, DataflowScope scope) {
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
        public Nullability visit(ASTArrayAccess node, DataflowScope scope) {
            // todo. actually we need to be able to query another analysis from here,
            //  to get the corresponding array model
            return UNKNOWN;
        }

        @Override
        public Nullability visit(ASTNullLiteral node, DataflowScope scope) {
            return NULL;
        }
    }
}
