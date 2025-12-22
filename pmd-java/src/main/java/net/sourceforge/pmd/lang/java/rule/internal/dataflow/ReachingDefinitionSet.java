/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr;

/**
 * A set of reaching definitions, ie the assignments that are visible
 * at some point. One can use {@link ReachingDefinitionsAnalysis.DataflowResult#getReachingDefinitions(ASTAssignableExpr.ASTNamedReferenceExpr)}
 * to get the data flow that reaches a variable usage (and go backwards with
 * the {@linkplain ReachingDefinitionsAnalysis.DataflowResult#getKillers(AssignmentEntry) kill record}).
 */
public final class ReachingDefinitionSet {

    private static final ReachingDefinitionSet UNKNOWN = new ReachingDefinitionSet();
    private static final ReachingDefinitionSet EMPTY_KNOWN = new ReachingDefinitionSet(emptySet());

    private Set<AssignmentEntry> reaching;
    private boolean isNotFullyKnown;
    private boolean containsInitialFieldValue;


    static {
        assert !EMPTY_KNOWN.isNotFullyKnown();
        assert UNKNOWN.isNotFullyKnown();
    }

    private ReachingDefinitionSet() {
        this.reaching = emptySet();
        this.containsInitialFieldValue = false;
        this.isNotFullyKnown = true;
    }

    ReachingDefinitionSet(/*Mutable*/Set<AssignmentEntry> reaching) {
        this.reaching = reaching;
        this.containsInitialFieldValue = reaching.removeIf(AssignmentEntry::isFieldAssignmentAtStartOfMethod);
        // not || as we want the side effect
        this.isNotFullyKnown = containsInitialFieldValue | reaching.removeIf(AssignmentEntry::isUnbound);
    }

    /** Returns the set of assignments that may reach the place. */
    public Set<AssignmentEntry> getReaching() {
        return Collections.unmodifiableSet(reaching);
    }

    /**
     * Returns true if there were some {@linkplain AssignmentEntry#isUnbound() unbound}
     * assignments in this set. They are not part of {@link #getReaching()}.
     */
    public boolean isNotFullyKnown() {
        return isNotFullyKnown;
    }

    /**
     * Contains a {@link AssignmentEntry#isFieldAssignmentAtStartOfMethod()}.
     * They are not part of {@link #getReaching()}.
     */
    public boolean containsInitialFieldValue() {
        return containsInitialFieldValue;
    }

    void absorb(ReachingDefinitionSet reaching) {
        this.containsInitialFieldValue |= reaching.containsInitialFieldValue;
        this.isNotFullyKnown |= reaching.isNotFullyKnown;
        if (this.reaching.isEmpty()) { // unmodifiable
            this.reaching = new LinkedHashSet<>(reaching.reaching);
        } else {
            this.reaching.addAll(reaching.reaching);
        }
    }

    public static ReachingDefinitionSet unknown() {
        return new ReachingDefinitionSet();
    }

    public static ReachingDefinitionSet blank() {
        return new ReachingDefinitionSet(emptySet());
    }
}
