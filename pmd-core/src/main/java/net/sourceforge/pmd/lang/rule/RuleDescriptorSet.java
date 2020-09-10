/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The new ruleset.
 */
public class RuleDescriptorSet {


    private final Set<RuleDescriptor> rules;

    RuleDescriptorSet(Collection<? extends RuleDescriptor> rules) {
        this.rules = new LinkedHashSet<>(rules);
    }

}
