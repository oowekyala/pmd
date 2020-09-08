/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;


class RuleDefDescriptor extends BaseRuleDescriptor {

    private final String languageId;
    RuleDefDescriptor(RuleDescriptorConfig.RuleDefConfig builder) {
        super(builder);
        this.languageId = builder.languageId;
    }

    @Override
    public String getLanguageId() {
        return languageId;
    }
}
