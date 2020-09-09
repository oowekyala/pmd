/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.internal;

import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.benchmark.TimeTracker;
import net.sourceforge.pmd.benchmark.TimedOperation;
import net.sourceforge.pmd.benchmark.TimedOperationCategory;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.internal.RunnableRuleSet.RunnableRule;
import net.sourceforge.pmd.reporting.FileAnalysisListener;

/** Applies a set of rules to a set of ASTs. */
public class RuleApplicator {

    // we reuse the type lattice from run to run, eventually it converges
    // towards the final topology (all node types have been encountered)
    // This has excellent performance! Indexing time is insignificant
    // compared to rule application for any non-trivial ruleset. Even
    // when you use a single rule, indexing time is insignificant compared
    // to eg type resolution.

    private final TreeIndex idx;

    public RuleApplicator(TreeIndex index) {
        this.idx = index;
    }


    public void index(Collection<? extends Node> nodes) {
        idx.reset();
        for (Node root : nodes) {
            indexTree(root, idx);
        }
    }

    public void apply(RunnableRuleSet rules, FileAnalysisListener listener) {
        applyOnIndex(idx, rules, listener);
    }

    private void applyOnIndex(TreeIndex idx, RunnableRuleSet rules, FileAnalysisListener listener) {
        for (RunnableRule rule : rules.getRules()) {
            rule.getAnalyser().nextFile();
            RuleContext ctx = RuleContext.create(listener, rule.getDescriptor());

            Iterator<? extends Node> targets = rule.getTargetSelector().getVisitedNodes(idx);
            while (targets.hasNext()) {
                Node node = targets.next();
                if (rule.appliesToVersion(node.getLanguageVersion())) {
                    continue;
                }

                try (TimedOperation rcto = TimeTracker.startOperation(TimedOperationCategory.RULE, rule.getDescriptor().getName())) {
                    rule.getAnalyser().apply(node, ctx);
                    rcto.close(1);
                } catch (RuntimeException e) {
                    // The listener handles logging if needed,
                    // it may also rethrow the error.
                    listener.onError(new ProcessingError(e, node.getSourceCodeFile()));
                }
            }
        }
    }

    private void indexTree(Node top, TreeIndex idx) {
        idx.indexNode(top);
        for (Node child : top.children()) {
            indexTree(child, idx);
        }
    }

    public static RuleApplicator build(Iterator<? extends RunnableRule> rules) {
        TargetSelectorInternal.ApplicatorBuilder builder = new TargetSelectorInternal.ApplicatorBuilder();
        rules.forEachRemaining(it -> it.getTargetSelector().prepare(builder));
        return builder.build();
    }

}
