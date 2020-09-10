/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.internal;

import java.text.MessageFormat;
import java.util.logging.Logger;

import net.sourceforge.pmd.lang.rule.RuleBehavior.DysfunctionalRuleException;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleInitializationWarner;

/**
 *
 */
public class LogRuleInitializationWarner implements RuleInitializationWarner {

    private final Logger logger;
    private final boolean warn;

    public LogRuleInitializationWarner(Logger logger, boolean warn) {
        this.logger = logger;
        this.warn = warn;
    }

    @Override
    public void configWarning(String message, Object... args) {
        if (warn) {
            logger.warning(formatMessage(message, args));
        }
    }

    @Override
    public DysfunctionalRuleException fatalConfigError(String message, Object... args) throws DysfunctionalRuleException {
        throw new DysfunctionalRuleException(formatMessage(message, args));
    }

    private String formatMessage(String message, Object[] args) {
        return MessageFormat.format(message, args);
    }
}
