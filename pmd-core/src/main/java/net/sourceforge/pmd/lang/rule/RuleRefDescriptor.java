/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;


import java.util.List;

import net.sourceforge.pmd.RulePriority;

class RuleRefDescriptor extends BaseRuleDescriptor {

    private final RuleDescriptor baseDescriptor;

    RuleRefDescriptor(RuleDescriptorConfig.RuleRefConfig config) {
        super(config);
        this.baseDescriptor = config.referencedRule;
    }

    @Override
    public String getLanguageId() {
        return baseDescriptor.getLanguageId();
    }

    @Override
    public String getName() {
        return name == null ? baseDescriptor.getName() : name;
    }

    @Override
    public boolean isDeprecated() {
        return isDeprecated == null ? baseDescriptor.isDeprecated() : isDeprecated;
    }

    @Override
    public String getSince() {
        return since == null ? baseDescriptor.getSince() : since;
    }

    @Override
    public String getRuleSetName() {
        return ruleSetName == null ? baseDescriptor.getRuleSetName() : ruleSetName;
    }

    @Override
    public String getMessage() {
        return message == null ? baseDescriptor.getMessage() : message;
    }

    @Override
    public String getDescription() {
        return description == null ? baseDescriptor.getDescription() : description;
    }

    @Override
    public List<String> getExamples() {
        return examples == null ? baseDescriptor.getExamples() : examples;
    }

    @Override
    public String getExternalInfoUrl() {
        return externalInfoUrl == null ? baseDescriptor.getExternalInfoUrl() : externalInfoUrl;
    }

    @Override
    public RulePriority getPriority() {
        return priority == null ? baseDescriptor.getPriority() : priority;
    }


}
