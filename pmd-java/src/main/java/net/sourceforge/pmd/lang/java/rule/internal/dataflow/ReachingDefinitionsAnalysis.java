/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.AccessType;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchBranch;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchFallthroughBranch;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedAssignmentRule;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.DataMap;
import net.sourceforge.pmd.util.DataMap.SimpleDataKey;
import net.sourceforge.pmd.util.OptionalBool;

/**
 * A reaching definition analysis. This may be used to check whether
 * eg a value escapes, or is overwritten on all code paths.
 */
public final class ReachingDefinitionsAnalysis {

    // todo probably, make that non-optional. It would be useful to implement
    //  the flow-sensitive scopes of pattern variables

    // todo things missing for full coverage of the JLS:
    //  - follow `this(...)` constructor calls
    //  - treat `while(true)` and `do while(true)` specially

    //  see also the todo comments in UnusedAssignmentRule

    private static final SimpleDataKey<DataflowResult> DATAFLOW_RESULT_K = DataMap.simpleDataKey("java.dataflow.global");
    private static final SimpleDataKey<ReachingDefinitionSet> REACHING_DEFS = DataMap.simpleDataKey("java.dataflow.reaching.backwards");
    static final SimpleDataKey<AssignmentEntry> VAR_DEFINITION = DataMap.simpleDataKey("java.dataflow.field.def");
    private static final SimpleDataKey<OptionalBool> SWITCH_BRANCH_FALLS_THROUGH = DataMap.simpleDataKey("java.dataflow.switch.fallthrough");
    
    private ReachingDefinitionsAnalysis() {
        // utility class
    }

    /**
     * Returns the info computed by the dataflow pass for the given file.
     * The computation is done at most once.
     */
    public static DataflowResult getDataflowResult(ASTCompilationUnit acu) {
        return acu.getUserMap().computeIfAbsent(DATAFLOW_RESULT_K, () -> process(acu));
    }

    /**
     * If the var id is that of a field, returns the assignment entry that
     * corresponds to its definition (either blank or its initializer). From
     * there, using the kill record, we can draw the graph of all assignments.
     * Returns null if not a field, or the compilation unit has not been processed.
     */
    public static @Nullable AssignmentEntry getFieldDefinition(ASTVariableId varId) {
        if (!varId.isField()) {
            return null;
        }
        return varId.getUserMap().get(VAR_DEFINITION);
    }

    private static DataflowResult process(ASTCompilationUnit node) {
        DataflowResult dataflowResult = new DataflowResult();
        for (ASTTypeDeclaration typeDecl : node.getTypeDeclarations()) {
            KillTrackerState subResult = new KillTrackerState();
            BaseDataflowPass.processTypeDecl(typeDecl, subResult);
            if (subResult.usedAssignments.size() < subResult.allAssignments.size()) {
                Set<AssignmentEntry> unused = subResult.allAssignments;
                unused.removeAll(subResult.usedAssignments);
                unused.removeIf(AssignmentEntry::isUnbound);
                unused.removeIf(AssignmentEntry::isFieldDefaultValue);
                dataflowResult.unusedAssignments.addAll(unused);
            }

            CollectionUtil.mergeMaps(
                dataflowResult.killRecord,
                subResult.killRecord,
                (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
        }

        return dataflowResult;
    }

    /**
     * Global result of the dataflow analysis.
     */
    // this is a façade class
    public static final class DataflowResult {

        final Set<AssignmentEntry> unusedAssignments;
        final Map<AssignmentEntry, Set<AssignmentEntry>> killRecord;


        DataflowResult() {
            this.unusedAssignments = new LinkedHashSet<>();
            this.killRecord = new LinkedHashMap<>();
        }

        /**
         * To be interpreted by {@link  UnusedAssignmentRule}.
         */
        public Set<AssignmentEntry> getUnusedAssignments() {
            return Collections.unmodifiableSet(unusedAssignments);
        }

        /**
         * May be useful to check for reassignment.
         */
        public @NonNull Set<AssignmentEntry> getKillers(AssignmentEntry assignment) {
            return killRecord.getOrDefault(assignment, emptySet());
        }

        // These methods are only valid to be called if the dataflow pass has run.
        // This is why they are instance methods here: by asking for the DataflowResult
        // instance to get access to them, you ensure that the pass has been executed properly.

        /**
         * Returns whether the switch branch falls-through to the next one (or the end of the switch).
         */
        public @NonNull OptionalBool switchBranchFallsThrough(ASTSwitchBranch b) {
            if (b instanceof ASTSwitchFallthroughBranch) {
                return Objects.requireNonNull(b.getUserMap().get(SWITCH_BRANCH_FALLS_THROUGH));
            }
            return OptionalBool.NO;
        }


        public @NonNull ReachingDefinitionSet getReachingDefinitions(ASTNamedReferenceExpr expr) {
            return expr.getUserMap().computeIfAbsent(REACHING_DEFS, () -> reachingFallback(expr));
        }

        // Fallback, to compute reaching definitions for some nodes
        // that are not tracked by the tree exploration. Final fields
        // indeed have a fully known set of reaching definitions.
        private @NonNull ReachingDefinitionSet reachingFallback(ASTNamedReferenceExpr expr) {
            JVariableSymbol sym = expr.getReferencedSym();
            if (sym == null || sym.isField() && !sym.isFinal()) {
                return ReachingDefinitionSet.unknown();
            } else if (!sym.isField()) {
                ASTVariableId node = sym.tryGetNode();
                assert node != null
                    : "Not a field, and symbol is known, so should be a local which has a node";
                if (node.isLocalVariable()) {
                    assert node.getInitializer() == null : "Should be a blank local variable";
                    return ReachingDefinitionSet.blank();
                } else {
                    // Formal parameter or other kind of def which has
                    // an implicit initializer.
                    return ReachingDefinitionSet.unknown();
                }
            }

            ASTVariableId node = sym.tryGetNode();
            if (node == null) {
                return ReachingDefinitionSet.unknown(); // we don't care about non-local declarations
            }
            Set<AssignmentEntry> assignments = node.getLocalUsages()
                                                   .stream()
                                                   .filter(it -> it.getAccessType() == AccessType.WRITE)
                                                   .map(usage -> {
                                                       JavaNode parent = usage.getParent();
                                                       if (parent instanceof ASTUnaryExpression
                                                           && !((ASTUnaryExpression) parent).getOperator().isPure()) {
                                                           return parent;
                                                       } else if (usage.getIndexInParent() == 0
                                                           && parent instanceof ASTAssignmentExpression) {
                                                           return ((ASTAssignmentExpression) parent).getRightOperand();
                                                       } else {
                                                           return null;
                                                       }
                                                   }).filter(Objects::nonNull)
                                                   .map(it -> new AssignmentEntry(sym, node, it))
                                                   .collect(CollectionUtil.toMutableSet());

            ASTExpression init = node.getInitializer(); // this one is not in the usages
            if (init != null) {
                assignments.add(new AssignmentEntry(sym, node, init));
            }

            return new ReachingDefinitionSet(assignments);
        }
    }


    private static final class KillTrackerState extends BaseDataflowPass.GlobalAlgoState {

        final Set<AssignmentEntry> allAssignments;
        final Set<AssignmentEntry> usedAssignments;

        // track which assignments kill which
        // assignment -> killers(assignment)
        final Map<AssignmentEntry, Set<AssignmentEntry>> killRecord;


        private KillTrackerState(Set<AssignmentEntry> allAssignments,
                                 Set<AssignmentEntry> usedAssignments,
                                 Map<AssignmentEntry, Set<AssignmentEntry>> killRecord) {
            this.allAssignments = allAssignments;
            this.usedAssignments = usedAssignments;
            this.killRecord = killRecord;

        }

        private KillTrackerState() {
            this(new LinkedHashSet<>(),
                 new LinkedHashSet<>(),
                 new LinkedHashMap<>());
        }


        @Override
        protected void updateReachingDefs(@NonNull ASTNamedReferenceExpr reachingDefSink, JVariableSymbol var, BaseDataflowPass.VarLocalInfo info) {
            ReachingDefinitionSet reaching;
            if (info == null || var.isField() && var.isFinal()) {
                return;
            } else {
                reaching = new ReachingDefinitionSet(new LinkedHashSet<>(info.reachingDefs));
            }
            // need to merge into previous to account for cyclic control flow
            reachingDefSink.getUserMap().merge(REACHING_DEFS, reaching, (current, newer) -> {
                current.absorb(newer);
                return current;
            });
        }

        @Override
        protected void useVar(BaseDataflowPass.VarLocalInfo info) {
            usedAssignments.addAll(info.reachingDefs);
        }

        @Override
        protected void newAssignment(BaseDataflowPass.@Nullable VarLocalInfo previous, AssignmentEntry newEntry) {
            if (previous != null) {
                // those assignments were overwritten ("killed")
                for (AssignmentEntry killed : previous.reachingDefs) {
                    if (killed.isBlankLocal()) {
                        continue;
                    }

                    killRecord.computeIfAbsent(killed, k -> new LinkedHashSet<>(1))
                              .add(newEntry);
                }
            }
            allAssignments.add(newEntry);
        }

        @Override
        public void switchBranchFallsThrough(ASTSwitchBranch branch, OptionalBool isFallingThrough) {
            branch.getUserMap().set(SWITCH_BRANCH_FALLS_THROUGH, isFallingThrough);
        }
    }
}
