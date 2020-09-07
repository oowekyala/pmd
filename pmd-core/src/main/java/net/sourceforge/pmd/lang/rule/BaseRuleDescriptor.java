/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import net.sourceforge.pmd.RulePriority;

abstract class BaseRuleDescriptor implements RuleDescriptor {

    protected final String languageId;
    protected final String name;
    protected final String message;
    protected final String description;
    protected final Boolean isDeprecated;
    protected final String since;
    protected final String ruleSetName;
    protected final List<String> examples;
    protected final String externalInfoUrl;
    protected final RulePriority priority;

    BaseRuleDescriptor(RuleDescriptorConfig config) {
        this.languageId = config.languageId;
        this.name = config.name;
        this.description = config.description;
        this.ruleSetName = config.ruleSetName;
        this.message = config.message;
        this.examples = config.examples;
        this.externalInfoUrl = config.externalInfoUrl;
        this.priority = config.priority;
        this.isDeprecated = config.isDeprecated;
        this.since = config.since;
    }

    @Override
    public String getLanguageId() {
        return languageId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDeprecated() {
        return isDeprecated != null && isDeprecated;
    }

    @Override
    public String getSince() {
        return since;
    }

    @Override
    public String getRuleSetName() {
        return ruleSetName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getExamples() {
        return examples;
    }

    @Override
    public String getExternalInfoUrl() {
        return externalInfoUrl;
    }

    @Override
    public RulePriority getPriority() {
        return priority;
    }

    public abstract static class RuleDescriptorConfig {

        private String languageId;
        private String name;
        private String description;
        private String ruleSetName;
        private String message;
        private List<String> examples;
        private String externalInfoUrl;
        private RulePriority priority;
        private Boolean isDeprecated;
        private String since;

        public RuleDescriptorConfig setLanguageId(String languageId) {
            this.languageId = languageId;
            return this;
        }

        public RuleDescriptorConfig setName(String name) {
            this.name = name;
            return this;
        }

        public RuleDescriptorConfig setDescription(String description) {
            this.description = description;
            return this;
        }

        public RuleDescriptorConfig setRuleSetName(String ruleSetName) {
            this.ruleSetName = ruleSetName;
            return this;
        }

        public RuleDescriptorConfig setMessage(String message) {
            this.message = message;
            return this;
        }

        public RuleDescriptorConfig setExamples(List<String> examples) {
            this.examples = examples;
            return this;
        }

        public RuleDescriptorConfig setExternalInfoUrl(String externalInfoUrl) {
            this.externalInfoUrl = externalInfoUrl;
            return this;
        }

        public RuleDescriptorConfig setPriority(RulePriority priority) {
            this.priority = priority;
            return this;
        }

        public RuleDescriptorConfig setDeprecated(boolean isDeprecated) {
            this.isDeprecated = isDeprecated;
            return this;
        }

        public RuleDescriptorConfig setSince(String since) {
            this.since = since;
            return this;
        }

        public abstract RuleDescriptor build();

    }
}
