/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.benchmark.TimeTracker;
import net.sourceforge.pmd.benchmark.TimedOperation;
import net.sourceforge.pmd.benchmark.TimedOperationCategory;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.rule.ConfiguredRuleDescriptor;
import net.sourceforge.pmd.lang.rule.RuleBehavior;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleAnalyser;
import net.sourceforge.pmd.lang.rule.RuleDescriptorSet;
import net.sourceforge.pmd.reporting.FileAnalysisListener;

/**
 *
 */
public class RunnableRuleSet {

    private final Set<RunnableRule> rules;
    private final RuleApplicator ruleApplicator;

    public RunnableRuleSet(Set<RunnableRule> rules) {
        this.rules = new LinkedHashSet<>(rules);
        this.ruleApplicator = RuleApplicator.build(rules.stream().map(RunnableRule::getBehavior).iterator());
    }


    public void apply(RootNode root, FileAnalysisListener listener) {
        try (TimedOperation to = TimeTracker.startOperation(TimedOperationCategory.RULE_AST_INDEXATION)) {
            ruleApplicator.index(Collections.singletonList(root));
        }

        ruleApplicator.apply(ruleSet.getRules(), listener);


        for (RuleSet ruleSet : ruleSets) {
            if (ruleSet.applies(file)) {
                ruleApplicator.apply(ruleSet.getRules(), listener);
            }
        }
    }

    public Iterable<RunnableRule> getRules() {
        return rules;
    }

    public static class RunnableRule {

        private final ConfiguredRuleDescriptor descriptor;
        private final RuleAnalyser analyser;


        public RunnableRule(ConfiguredRuleDescriptor descriptor, RuleAnalyser analyser) {
            this.descriptor = descriptor;
            this.analyser = analyser;
        }

        public ConfiguredRuleDescriptor getDescriptor() {
            return descriptor;
        }

        public RuleBehavior getBehavior() {
            return descriptor.getBehavior();
        }

        public RuleAnalyser getAnalyser() {
            return analyser;
        }
    }
}
