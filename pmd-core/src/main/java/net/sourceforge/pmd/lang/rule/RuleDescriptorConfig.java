/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/** Builder pattern for a descriptor. */
public abstract class RuleDescriptorConfig {

    String languageId;
    String name;
    String description;
    String ruleSetName;
    String message;
    List<String> examples;
    String externalInfoUrl;
    RulePriority priority;
    Boolean isDeprecated;
    String since;

    final RuleBehavior behavior;
    final PropertySource properties;

    private RuleDescriptorConfig(RuleBehavior behavior) {
        this.behavior = behavior;
        properties = new RuleProperties(behavior.declaredProperties());
    }

    static class RuleRefConfig extends RuleDescriptorConfig {

        final RuleDescriptor referencedRule;

        private RuleRefConfig(RuleDescriptor referencedRule) {
            super(referencedRule.behavior());
            this.referencedRule = referencedRule;

            for (PropertyDescriptor<?> prop : referencedRule.behavior().declaredProperties()) {
                // copy properties so far
                copyProperty(referencedRule, prop, this.properties);
            }
        }

        static <T> void copyProperty(RuleDescriptor source, PropertyDescriptor<T> descriptor, PropertySource target) {
            target.setProperty(descriptor, source.getProperty(descriptor));
        }


        @Override
        public RuleDescriptor build() {
            return new RuleRefDescriptor(this);
        }
    }

    static class RuleDefConfig extends RuleDescriptorConfig {

        final String languageId;

        private RuleDefConfig(RuleBehavior behavior, String languageId) {
            super(behavior);
            this.languageId = languageId;
        }

        @Override
        public RuleDescriptor build() {
            return new RuleDefDescriptor(this);
        }
    }

    /**
     * A new config for a rule reference, referencing the given descriptor.
     * The properties of the new descriptor are initialized to the values
     * they have on the base descriptor, but can then be independently
     * modified.
     */
    public static RuleDescriptorConfig forReference(RuleDescriptor baseDescriptor) {
        return new RuleRefConfig(baseDescriptor);
    }

    /**
     * A new config for a rule definition. Note: many methods are required.
     */
    public static RuleDescriptorConfig forDefinition(String languageId, RuleBehavior behavior) {
        return new RuleDefConfig(behavior, languageId);
    }

    public <T> RuleDescriptorConfig setProperty(PropertyDescriptor<T> descriptor, T value) {
        properties.setProperty(descriptor, value);
        return this;
    }

    public RuleDescriptorConfig languageId(String languageId) {
        this.languageId = languageId;
        return this;
    }

    public abstract RuleDescriptor build();

}
