/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;


import java.util.Objects;

class RuleDefDescriptor extends BaseRuleDescriptor {

    private final RuleBehavior behavior;

    RuleDefDescriptor(RuleDescriptorConfig builder, RuleBehavior behavior) {
        super(builder);
        this.behavior = behavior;
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(name);
        Objects.requireNonNull(languageId);
        Objects.requireNonNull(description);
        // todo etc
    }

    @Override
    public RuleBehavior getBehavior() {
        return behavior;
    }
}
