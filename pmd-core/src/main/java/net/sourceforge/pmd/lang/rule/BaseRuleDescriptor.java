/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

abstract class BaseRuleDescriptor implements RuleDescriptor {

    // these may be null in RuleRefDescriptor
    // must be non-null in RuleDefDescriptor
    protected final String name;
    protected final String message;
    protected final String description;
    protected final Boolean isDeprecated;
    protected final String since;
    protected final String ruleSetName;
    protected final List<String> examples;
    protected final String externalInfoUrl;
    protected final RulePriority priority;

    private final RuleBehavior behavior;
    private final PropertySource properties;

    BaseRuleDescriptor(RuleDescriptorConfig config) {
        this.name = config.name;
        this.description = config.description;
        this.ruleSetName = config.ruleSetName;
        this.message = config.message;
        this.examples = config.examples;
        this.externalInfoUrl = config.externalInfoUrl;
        this.priority = config.priority;
        this.isDeprecated = config.isDeprecated;
        this.since = config.since;
        this.behavior = AssertionUtil.requireParamNotNull("behavior", config.behavior);
        this.properties = AssertionUtil.requireParamNotNull("properties", config.properties);
    }

    @Override
    public <T> T getProperty(PropertyDescriptor<T> propertyDescriptor) {
        return properties.getProperty(propertyDescriptor);
    }

    @Override
    public PropertyDescriptor<?> getPropertyDescriptor(String name) {
        return properties.getPropertyDescriptor(name);
    }

    @Override
    public final RuleBehavior behavior() {
        return behavior;
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

}
