/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.NONNULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability.UNKNOWN;

import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTArrayAllocation;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTCastExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
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
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTSuperExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTThisExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.rule.internal.DataflowPass;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.DfAnalysis;
import net.sourceforge.pmd.lang.java.rule.internal.dataflow.ValueModel.Nullability;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

public class NullabilityProblemRule extends AbstractJavaRule {


    @Override
    public Object visit(ASTCompilationUnit node, Object data) {

        DataflowPass.DataflowResult df = DataflowPass.getDataflowResult(node);

        NullabilityAnalysis analysis = new NullabilityAnalysis(df);
        node.descendants(ASTExpression.class).crossFindBoundaries()
            .forEach(it -> {
                if (it instanceof ASTVariableAccess
                    && !JavaAstUtils.isVarAccessStrictlyWrite((ASTVariableAccess) it)
                    && analysis.getNullability(it) == NULL) {
                    asCtx(data).addViolationWithMessage(it, "Variable `{0}` is always null", ((ASTVariableAccess) it).getName());
                }
            });
        return null;
    }

    boolean expressionWillNpeIfNull(ASTExpression e) {
        if (e.getParent() instanceof ASTPrimaryExpression && e.getIndexInParent() == 0) {
            // e.method()
            // e.field
            // e::method
            // e[index]
            return true;
        } else if (e.getParent() instanceof ASTSwitchExpression) { // todo null tolerance
            return true;
        }
        return false;
    }

    static class NullabilityAnalysis extends DfAnalysis<Nullability> {

        NullabilityAnalysis(DataflowPass.DataflowResult dataflow) {
            super(dataflow);
        }

        Nullability getNullability(ASTExpression e) {
            return getModel(e);
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
        protected Nullability getModelOfReachingDefinitions(ASTNamedReferenceExpr node) {
            return super.getModelOfReachingDefinitions(node);
        }
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
            if (JavaAstUtils.isBooleanLiteral(node)) {
                if (JavaAstUtils.isBooleanLiteral(node, true)) {
                    return data.getNullability(node.getThenBranch());
                }
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
            return UNKNOWN; // todo
        }

        @Override
        public Nullability visit(ASTVariableAccess node, NullabilityAnalysis data) {
            return data.getModelOfReachingDefinitions(node);
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
