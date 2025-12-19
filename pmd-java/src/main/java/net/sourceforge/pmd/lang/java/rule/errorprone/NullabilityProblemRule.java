/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import static net.sourceforge.pmd.lang.java.rule.errorprone.NullabilityProblemRule.NullabilityKind.NULL;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import net.sourceforge.pmd.lang.java.ast.ASTArrayAccess;
import net.sourceforge.pmd.lang.java.ast.ASTArrayAllocation;
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
import net.sourceforge.pmd.lang.java.ast.ASTSuperExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTThisExpression;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.rule.internal.DataflowPass;
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

    static class NullabilityAnalysis {
        final DataflowPass.DataflowResult dataflow;
        final Map<ASTExpression, NullabilityKind> exprToNullability = new HashMap<>();

        NullabilityAnalysis(DataflowPass.DataflowResult dataflow) {
            this.dataflow = dataflow;
        }

        NullabilityKind getNullability(ASTExpression e) {
            // cannot use computeifabsent because of ooncurrent modification
            NullabilityKind nullability = exprToNullability.get(e);
            if (nullability == null) {
                nullability = e.acceptVisitor(NullabilityVisitor.INSTANCE, this);
                exprToNullability.put(e, nullability);
            }
            return nullability;
        }

        NullabilityKind getNullabilityOfSymbolHere(ASTVariableAccess node) {
            if (dataflow.getReachingDefinitions(node).isNotFullyKnown()) {
                return NullabilityKind.UNKNOWN;
            }
            // todo flow sensitivity is really important if we want to
            //  report something else than EMPTY
            NullabilityKind result = NullabilityKind.EMPTY;
            for (DataflowPass.AssignmentEntry a : dataflow.getReachingDefinitions(node).getReaching()) {
                NullabilityKind nullability = getNullabilityOfAssignment(a);
                result = result.join(nullability);
            }
            return result;
        }

        NullabilityKind getNullabilityOfType(JTypeMirror ty) {
            return NullabilityKind.UNKNOWN; // todo use annotations
        }

        private NullabilityKind getNullabilityOfAssignment(DataflowPass.AssignmentEntry a) {
            ASTExpression rhs = a.getRhsAsExpression();
            if (rhs != null) {
                return getNullability(rhs);
            }
            if (a.isFieldDefaultValue()) {
                return a.getDeclaredType().isPrimitive() ? NullabilityKind.NONNULL
                                                         : NULL;
            } else if (a.isFormalParameterInitialValue()) {
                return getNullabilityOfType(a.getDeclaredType());
            } else if (a.isUnbound()) {
                return NullabilityKind.UNKNOWN;
            } else if (a.isForeachVar()) {
                // todo type may not have explicit annotation but the
                //  annotation may be in the iterable type
                return getNullabilityOfType(a.getDeclaredType());
            } else if (a.isBlankDeclaration()) {
                return NullabilityKind.EMPTY;
            }
            return NullabilityKind.UNKNOWN;
        }
    }

    static class NullabilityVisitor extends JavaVisitorBase<NullabilityAnalysis, NullabilityKind> {

        static final NullabilityVisitor INSTANCE = new NullabilityVisitor();


        @Override
        public NullabilityKind visitExpression(ASTExpression node, NullabilityAnalysis data) {
            throw new NotImplementedException("there should be an override here for " + node.getXPathNodeName());
        }

        @Override
        public NullabilityKind visit(ASTLambdaExpression node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTAssignmentExpression node, NullabilityAnalysis data) {
            return data.getNullability(node.getRightOperand());
        }

        @Override
        public NullabilityKind visit(ASTConditionalExpression node, NullabilityAnalysis data) {
            if (JavaAstUtils.isBooleanLiteral(node)) {
                if (JavaAstUtils.isBooleanLiteral(node, true)) {
                    return data.getNullability(node.getThenBranch());
                }
                return data.getNullability(node.getElseBranch());
            }
            NullabilityKind thenNull = data.getNullability(node.getThenBranch());
            NullabilityKind elseNull = data.getNullability(node.getElseBranch());
            return thenNull.join(elseNull);
        }


        @Override
        public NullabilityKind visit(ASTInfixExpression node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }


        @Override
        public NullabilityKind visit(ASTUnaryExpression node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTCastExpression node, NullabilityAnalysis data) {
            return data.getNullability(node.getOperand());
        }


        @Override
        public NullabilityKind visit(ASTSwitchExpression node, NullabilityAnalysis data) {
            return NullabilityKind.UNKNOWN; // TODO
        }


    /*
        Primaries
     */

        @Override
        public NullabilityKind visit(ASTMethodCall node, NullabilityAnalysis data) {
            return visitPrimaryExpr(node, data);
        }

        @Override
        public NullabilityKind visit(ASTConstructorCall node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTArrayAllocation node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTArrayAccess node, NullabilityAnalysis data) {
            return NullabilityKind.UNKNOWN; // todo
        }

        @Override
        public NullabilityKind visit(ASTVariableAccess node, NullabilityAnalysis data) {
            return data.getNullabilityOfSymbolHere(node);
        }

        @Override
        public NullabilityKind visit(ASTFieldAccess node, NullabilityAnalysis data) {
            return NullabilityKind.UNKNOWN; // todo
        }


        @Override
        public NullabilityKind visit(ASTMethodReference node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }


        @Override
        public NullabilityKind visit(ASTThisExpression node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTSuperExpression node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTClassLiteral node, NullabilityAnalysis data) {
            return NullabilityKind.NONNULL;
        }

        public NullabilityKind visitLiteral(ASTLiteral node, NullabilityAnalysis data) {
            // Other literals delegate here
            return NullabilityKind.NONNULL;
        }

        @Override
        public NullabilityKind visit(ASTNullLiteral node, NullabilityAnalysis data) {
            return NULL;
        }
    }

    enum NullabilityKind {
        EMPTY,
        NULL,
        NONNULL,
        NULLABLE,
        UNKNOWN;

        NullabilityKind join(NullabilityKind other) {
            if (this == NULL && other == NONNULL
                || this == NONNULL && other == NULL) {
                return NULLABLE;
            }
            return this.compareTo(other) < 0 ? other : this;
        }
    }

    static class NullabilityStatus {
        NullabilityKind kind;
        DataflowPass.ReachingDefinitionSet reaching;

    }
}
