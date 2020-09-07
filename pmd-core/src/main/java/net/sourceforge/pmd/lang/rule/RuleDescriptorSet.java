/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.rule.RuleBehavior.DysfunctionalRuleException;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleAnalyser;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleInitializationWarner;
import net.sourceforge.pmd.lang.rule.internal.RunnableRuleSet;
import net.sourceforge.pmd.lang.rule.internal.RunnableRuleSet.RunnableRule;

/**
 * The new ruleset.
 */
public class RuleDescriptorSet {

    private static final Logger LOG = Logger.getLogger("Ruleset initialization");

    private final Set<ConfiguredRuleDescriptor> rules;

    RuleDescriptorSet(Collection<? extends ConfiguredRuleDescriptor> rules) {
        this.rules = new LinkedHashSet<>(rules);
    }

    RunnableRuleSet initialize(LanguageRegistry languages) {
        Set<RunnableRule> result = new LinkedHashSet<>();

        RuleInitializationWarner warner = new LogRuleInitializationWarner();
        for (ConfiguredRuleDescriptor rule : rules) {
            try {

                RuleAnalyser analyser = rule.initialize(languages, warner);
                result.add(new RunnableRule(rule, analyser));
            } catch (DysfunctionalRuleException e) {
                LOG.warning("Removing dysfunctional rule " + rule.getName());
                LOG.warning(e.getMessage());
            }
        }

        return new RunnableRuleSet(result);
    }


    private static class LogRuleInitializationWarner implements RuleInitializationWarner {


        @Override
        public void configWarning(String message, Object... args) {
            LOG.warning(MessageFormat.format(message, args));
        }

        @Override
        public DysfunctionalRuleException fatalConfigError(String message, Object... args) throws DysfunctionalRuleException {
            String formatted = MessageFormat.format(message, args);
            return new DysfunctionalRuleException(formatted);
        }
    }
}
