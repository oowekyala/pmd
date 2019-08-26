/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;

/**
 * @author Clément Fournier
 */
public interface ViolationReporter {



    default void addViolation() {
        RuleContext ruleContext = (RuleContext) data;
        ruleContext.getLanguageVersion().getLanguageVersionHandler().getRuleViolationFactory().addViolation(ruleContext,
                                                                                                            this, node, this.getMessage(), null);
    }









}
