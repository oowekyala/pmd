/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;


import net.sourceforge.pmd.lang.rule.RuleDescriptorBuilder.RuleDefBuilder;

class RuleDefDescriptor extends BaseRuleDescriptor {

    private final String languageId;
    RuleDefDescriptor(RuleDefBuilder builder) {
        super(builder);
        this.languageId = builder.languageId;
        // todo assert everything is non-null?
    }

    @Override
    public String getLanguageId() {
        return languageId;
    }
}
