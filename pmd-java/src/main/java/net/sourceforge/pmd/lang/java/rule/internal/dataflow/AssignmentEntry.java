/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.symbols.JFieldSymbol;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.Substitution;

public class AssignmentEntry implements Comparable<AssignmentEntry> {

    final JVariableSymbol var;
    final ASTVariableId node;

    // this is not necessarily an expression, it may be also the
    // variable declarator of a foreach loop
    final JavaNode rhs;

    AssignmentEntry(JVariableSymbol var, ASTVariableId node, JavaNode rhs) {
        this.var = var;
        this.node = node;
        this.rhs = rhs;
        // This may be overwritten repeatedly in loops, we probably don't care,
        // as normally they're created equal
        // Also for now we don't support getting a field.
        if ((isInitializer() || isBlankDeclaration()) && !isUnbound()) {
            node.getUserMap().set(ReachingDefinitionsAnalysis.VAR_DEFINITION, this);
        }
    }

    public boolean isInitializer() {
        return rhs.getParent() instanceof ASTVariableDeclarator
               && rhs.getIndexInParent() > 0;
    }

    public boolean isBlankDeclaration() {
        return rhs instanceof ASTVariableId;
    }

    public boolean isFieldDefaultValue() {
        return isBlankDeclaration() && isField();
    }

    public boolean isFormalParameterInitialValue() {
        return isBlankDeclaration() && ((ASTVariableId) rhs).isFormalParameter();
    }

    /**
     * A blank local that has no value (ie not a catch param or formal).
     */
    public boolean isBlankLocal() {
        return isBlankDeclaration() && node.isLocalVariable();
    }

    public boolean isUnaryReassign() {
        return rhs instanceof ASTUnaryExpression
               && getVarIfUnaryAssignment((ASTUnaryExpression) rhs) == var; // NOPMD #3205
    }

    private static JVariableSymbol getVarIfUnaryAssignment(ASTUnaryExpression node) {
        ASTExpression operand = node.getOperand();
        if (!node.getOperator().isPure() && operand instanceof ASTAssignableExpr.ASTNamedReferenceExpr) {
            return ((ASTAssignableExpr.ASTNamedReferenceExpr) operand).getReferencedSym();
        }
        return null;
    }

    @Override
    public int compareTo(AssignmentEntry o) {
        return this.rhs.compareLocation(o.rhs);
    }

    public int getLine() {
        return getLocation().getBeginLine();
    }

    public boolean isField() {
        return var instanceof JFieldSymbol;
    }

    public boolean isForeachVar() {
        return node.isForeachVariable();
    }

    public ASTVariableId getVarId() {
        return node;
    }


    public JavaNode getLocation() {
        return rhs;
    }

    /**
     * Return the static type of the declaration of this variable.
     * This may be a type different from the RHS type.
     *
     * @see #getRhsType()
     */
    public JTypeMirror getDeclaredType() {
        return var.getTypeMirror(Substitution.EMPTY);
    }

    // todo i'm probably missing some

    /**
     * <p>Returns non-null for an assignment expression, eg for (a = b), returns b.
     * For (i++), returns (i++) and not (i), same for (i--).
     * Returns null if the assignment is, eg, the default value
     * of a field; the "blank" definition of a local variable,
     * exception parameter, formal parameter, foreach variable, etc.
     */
    public @Nullable ASTExpression getRhsAsExpression() {
        if (isUnbound() || isBlankDeclaration()) {
            return null;
        }
        if (rhs instanceof ASTExpression) {
            return (ASTExpression) rhs;
        }
        return null;
    }

    /**
     * Returns the type of the right-hand side if it is an explicit
     * expression, null if it cannot be determined or there is no
     * right-hand side to this expression. TODO test
     */
    public @Nullable JTypeMirror getRhsType() {
        /* test case
           List<A> as;
           for (Object o : as) {
             // the rhs type of o should be A
           }
         */
        if (isUnbound() || isBlankDeclaration()) {
            return null;
        } else if (rhs instanceof ASTExpression) {
            return ((TypeNode) rhs).getTypeMirror();
        }
        return null;
    }

    /**
     * If true, then this "assignment" is not real. We conservatively
     * assume that the variable may have been set to another value by
     * a call to some external code.
     *
     * @see #isFieldAssignmentAtEndOfCtor()
     * @see #isFieldAssignmentAtStartOfMethod()
     */
    public boolean isUnbound() {
        return false;
    }

    /**
     * If true, then this "assignment" is the placeholder value given
     * to an instance field before a method starts. This is a subset of
     * {@link #isUnbound()}.
     */
    public boolean isFieldAssignmentAtStartOfMethod() {
        return false;
    }

    /**
     * If true, then this "assignment" is the placeholder value given
     * to a non-final instance field after a ctor ends. This is a subset of
     * {@link #isUnbound()}.
     */
    public boolean isFieldAssignmentAtEndOfCtor() {
        return false;
    }

    @Override
    public String toString() {
        return var.getSimpleName() + " := " + rhs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssignmentEntry that = (AssignmentEntry) o;
        return Objects.equals(var, that.var)
               && Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return 31 * var.hashCode() + rhs.hashCode();
    }

    enum SpecialAssignmentKind {
        NOT_SPECIAL,
        UNKNOWN_METHOD_CALL,
        INITIAL_FIELD_VALUE;

        boolean shouldJoinWithPreviousAssignment() {
            return this == UNKNOWN_METHOD_CALL;
        }
    }

    static class UnboundAssignment extends AssignmentEntry {

        private final SpecialAssignmentKind kind;

        UnboundAssignment(JVariableSymbol var, ASTVariableId node, JavaNode rhs, SpecialAssignmentKind kind) {
            super(var, node, rhs);
            this.kind = kind;
        }

        @Override
        public boolean isUnbound() {
            return true;
        }

        @Override
        public boolean isFieldAssignmentAtStartOfMethod() {
            return kind == SpecialAssignmentKind.INITIAL_FIELD_VALUE;
        }

        @Override
        public boolean isFieldAssignmentAtEndOfCtor() {
            return rhs instanceof ASTTypeDeclaration;
        }
    }
}
