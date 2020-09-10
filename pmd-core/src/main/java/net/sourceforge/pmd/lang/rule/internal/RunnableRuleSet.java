/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.benchmark.TimeTracker;
import net.sourceforge.pmd.benchmark.TimedOperation;
import net.sourceforge.pmd.benchmark.TimedOperationCategory;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.rule.RuleBehavior;
import net.sourceforge.pmd.lang.rule.RuleBehavior.DysfunctionalRuleException;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleAnalyser;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleInitializationWarner;
import net.sourceforge.pmd.lang.rule.RuleDescriptor;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;
import net.sourceforge.pmd.reporting.FileAnalysisListener;

/**
 * This is the pendant of {@link RuleSets}. When it's created, it's
 * fully initialized and can be applied immediately.
 */
public class RunnableRuleSet {

    private static final Logger LOG = Logger.getLogger("Ruleset initialization");

    private final Set<RunnableRule> rules;
    private final RuleApplicator ruleApplicator;

    public RunnableRuleSet(Set<RunnableRule> rules) {
        this.rules = new LinkedHashSet<>(rules);
        this.ruleApplicator = RuleApplicator.build(rules.stream().iterator());
    }

    public boolean applies(File file) {
        return false; // todo
    }

    /**
     * Produce a runnable ruleset by initializing every rule.
     */
    public static RunnableRuleSet initialize(Collection<? extends RuleDescriptor> rules, LanguageRegistry languages, boolean warn) {
        Set<RunnableRule> result = new LinkedHashSet<>();

        RuleInitializationWarner warner = new LogRuleInitializationWarner(LOG, warn);
        for (RuleDescriptor rule : rules) {
            try {
                RuleAnalyser analyser = initializeRule(rule, languages, warner);
                result.add(new RunnableRule(rule, analyser));
            } catch (DysfunctionalRuleException e) {
                warner.configWarning("Removing dysfunctional rule {0}, reason: {1}", rule.getName(), e.getMessage());
            }
        }

        return new RunnableRuleSet(result);
    }


    private static RuleAnalyser initializeRule(RuleDescriptor descriptor, LanguageRegistry languageRegistry, RuleInitializationWarner warner) throws DysfunctionalRuleException {
        Language language = LanguageRegistry.findLanguageByTerseName(descriptor.getLanguageId());
        return descriptor.behavior().initialize(descriptor, language, warner);
    }


    public void apply(RootNode root, FileAnalysisListener listener) {
        try (TimedOperation to = TimeTracker.startOperation(TimedOperationCategory.RULE_AST_INDEXATION)) {
            ruleApplicator.index(Collections.singletonList(root));
        }

        // todo normally there's a check for whether the ruleset applies
        //  to the file.
        ruleApplicator.apply(this, listener);
    }

    public Iterable<RunnableRule> getRules() {
        return rules;
    }

    public static class RunnableRule {

        private final RuleDescriptor descriptor;
        private final RuleAnalyser analyser;
        private final RuleTargetSelector selector;


        public RunnableRule(RuleDescriptor descriptor, RuleAnalyser analyser) {
            this.descriptor = descriptor;
            this.analyser = analyser;
            this.selector = analyser.getTargetSelector();
        }

        public static RunnableRule fromPmd6(Rule rule) {
            return new RunnableRule(rule, rule);
        }

        public boolean appliesToVersion(LanguageVersion lv) {
            return getBehavior().appliesToVersion(lv);
        }

        public RuleTargetSelector getTargetSelector() {
            return selector;
        }

        public RuleDescriptor getDescriptor() {
            return descriptor;
        }

        public RuleBehavior getBehavior() {
            return descriptor.behavior();
        }

        public RuleAnalyser getAnalyser() {
            return analyser;
        }
    }
}
