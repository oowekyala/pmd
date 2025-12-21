/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.EMPTY;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NONNULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.NULL;
import static net.sourceforge.pmd.lang.java.rule.internal.dataflow.NullabilityAnalysis.Nullability.UNKNOWN;

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
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

public class NullabilityAnalysis extends DfAnalysis<Nullability> {

    public Nullability getNullability(ASTExpression e) {
        // fixme - provide API to get result from a given expression.
        //  Previously the analysis was caching results, now we have
        //  scopes that do that. But analyses should be able to set
        //  their result on the AST when we know the final scope, for
        //  consumption by rules.
        return unknown();
        // return getModel(e);
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
        return UNKNOWN; // todo use annotations
    }

    @Override
    protected Nullability createForeachVarModel(ASTVariableId varId, JTypeMirror varType, ASTExpression iterableExpr, DataflowScope scope) {
        // todo look at the iterable for annotations
        return createModelBasedOnType(varType);
    }

    @Override
    protected DfAnalysis<Nullability>.CreateExprModelVisitor createModelForSimpleExprVisitor() {
        return new NullabilityVisitor();
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
            // todo
            return unknown();
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
