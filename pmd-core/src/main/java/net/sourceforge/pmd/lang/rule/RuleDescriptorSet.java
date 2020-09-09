/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sourceforge.pmd.lang.Language;
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

    private final Set<RuleDescriptor> rules;

    RuleDescriptorSet(Collection<? extends RuleDescriptor> rules) {
        this.rules = new LinkedHashSet<>(rules);
    }

    RunnableRuleSet initialize(LanguageRegistry languages) {
        Set<RunnableRule> result = new LinkedHashSet<>();

        RuleInitializationWarner warner = new LogRuleInitializationWarner();
        for (RuleDescriptor rule : rules) {
            try {
                RuleAnalyser analyser = initializeRule(rule, languages, warner);
                result.add(new RunnableRule(rule, analyser));
            } catch (DysfunctionalRuleException e) {
                LOG.warning("Removing dysfunctional rule " + rule.getName());
                LOG.warning(e.getMessage());
            }
        }

        return new RunnableRuleSet(result);
    }


    private static RuleAnalyser initializeRule(RuleDescriptor descriptor, LanguageRegistry languageRegistry, RuleInitializationWarner warner) throws DysfunctionalRuleException {
        Language language = LanguageRegistry.findLanguageByTerseName(descriptor.getLanguageId());
        return descriptor.behavior().initialize(descriptor, language, warner);
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
