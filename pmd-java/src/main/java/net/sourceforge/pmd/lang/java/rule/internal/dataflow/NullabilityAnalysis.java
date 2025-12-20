/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.NONNULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.UNKNOWN;
import static net.sourceforge.pmd.util.CollectionUtil.listOf;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTArrayAllocation;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTCastExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodReference;
import net.sourceforge.pmd.lang.java.ast.ASTNullLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTSuperExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTThisExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

public class NullabilityAnalysis extends DfAnalysis<Nullability> {

    public Nullability getNullability(ASTExpression e) {
        return getModel(e);
    }

    @Override
    List<Class<? extends DfAnalysis<?>>> dependentAnalyses() {
        return listOf(BooleanValueAnalysis.class);
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
    public @Nullable Nullability createModel(@NonNull ASTExpression expr) {
        return expr.acceptVisitor(NullabilityVisitor.INSTANCE, this);
    }

    @Override
    protected @Nullable Nullability createFieldDefaultModel(ASTVariableId varId) {
        return varId.getTypeNode() instanceof ASTPrimitiveType ? null : NULL;
    }

    @Override
    protected @NonNull Nullability createModelBasedOnType(JTypeMirror type) {
        return UNKNOWN; // todo use annotations
    }

    @Override
    protected Nullability createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr) {
        // todo look at the iterable for annotations
        return createModelBasedOnType(varType);
    }

    @Override
    protected Nullability getModelOfReachingDefinitions(ASTAssignableExpr.ASTNamedReferenceExpr node) {
        return super.getModelOfReachingDefinitions(node);
    }

    static class NullabilityVisitor extends JavaVisitorBase<NullabilityAnalysis, Nullability> {

        static final NullabilityVisitor INSTANCE = new NullabilityVisitor();


        @Override
        public Nullability visitExpression(ASTExpression node, NullabilityAnalysis data) {
            throw new NotImplementedException("there should be an override here for " + node.getXPathNodeName());
        }

        @Override
        public Nullability visit(ASTLambdaExpression node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTAssignmentExpression node, NullabilityAnalysis data) {
            return data.getNullability(node.getRightOperand());
        }

        @Override
        public Nullability visit(ASTConditionalExpression node, NullabilityAnalysis data) {
            BooleanValueAnalysis boolAnalysis = data.getAnalysis(BooleanValueAnalysis.class);
            BooleanValueAnalysis.BooleanModel conditionBool = boolAnalysis.getBooleanModel(node.getCondition());
            if (conditionBool == BooleanValueAnalysis.BooleanModel.TRUE) {
                return data.getNullability(node.getThenBranch());
            } else if (conditionBool == BooleanValueAnalysis.BooleanModel.FALSE) {
                return data.getNullability(node.getElseBranch());
            }
            Nullability thenNull = data.getNullability(node.getThenBranch());
            Nullability elseNull = data.getNullability(node.getElseBranch());
            return thenNull.join(elseNull);
        }


        @Override
        public Nullability visit(ASTInfixExpression node, NullabilityAnalysis data) {
            return NONNULL;
        }


        @Override
        public Nullability visit(ASTUnaryExpression node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTCastExpression node, NullabilityAnalysis data) {
            return data.getNullability(node.getOperand());
        }


        @Override
        public Nullability visit(ASTSwitchExpression node, NullabilityAnalysis data) {
            return UNKNOWN; // TODO
        }


    /*
        Primaries
     */

        @Override
        public Nullability visit(ASTMethodCall node, NullabilityAnalysis data) {
            return visitPrimaryExpr(node, data);
        }

        @Override
        public Nullability visit(ASTConstructorCall node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTArrayAllocation node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTArrayAccess node, NullabilityAnalysis data) {
            // todo. actually we need to be able to query another analysis from here,
            //  to get the corresponding array model
            return UNKNOWN;
        }

        @Override
        public Nullability visit(ASTVariableAccess node, NullabilityAnalysis data) {
            return data.getModelOfReachingDefinitions(node); // todo same here actually
        }

        @Override
        public Nullability visit(ASTFieldAccess node, NullabilityAnalysis data) {
            return data.getModelOfReachingDefinitions(node);
        }


        @Override
        public Nullability visit(ASTMethodReference node, NullabilityAnalysis data) {
            return NONNULL;
        }


        @Override
        public Nullability visit(ASTThisExpression node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTSuperExpression node, NullabilityAnalysis data) {
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTClassLiteral node, NullabilityAnalysis data) {
            return NONNULL;
        }

        public Nullability visitLiteral(ASTLiteral node, NullabilityAnalysis data) {
            // Other literals delegate here
            return NONNULL;
        }

        @Override
        public Nullability visit(ASTNullLiteral node, NullabilityAnalysis data) {
            return NULL;
        }
    }
}
