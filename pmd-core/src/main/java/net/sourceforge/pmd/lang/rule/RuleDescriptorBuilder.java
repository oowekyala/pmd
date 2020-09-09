/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/** Builder pattern for a descriptor. */
public abstract class RuleDescriptorBuilder {

    String name;
    String description;
    String ruleSetName;
    String message;
    List<String> examples = new ArrayList<>();
    String externalInfoUrl;
    RulePriority priority;
    Boolean isDeprecated;
    String since;

    final RuleBehavior behavior;
    final PropertySource properties;

    private RuleDescriptorBuilder(RuleBehavior behavior) {
        this.behavior = behavior;
        properties = new RuleProperties(behavior.declaredProperties());
    }

    static class RuleRefBuilder extends RuleDescriptorBuilder {

        final RuleDescriptor referencedRule;

        private RuleRefBuilder(RuleDescriptor referencedRule) {
            super(referencedRule.behavior());
            this.referencedRule = referencedRule;

            for (PropertyDescriptor<?> prop : referencedRule.behavior().declaredProperties()) {
                // copy properties so far
                PropertySource.copyProperty(referencedRule, prop, this.properties);
            }
        }

        @Override
        public RuleDescriptor build() {
            return new RuleRefDescriptor(this);
        }
    }

    static class RuleDefBuilder extends RuleDescriptorBuilder {

        final String languageId;

        private RuleDefBuilder(RuleBehavior behavior, String languageId) {
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
    public static RuleDescriptorBuilder forReference(RuleDescriptor baseDescriptor) {
        return new RuleRefBuilder(baseDescriptor);
    }

    /**
     * A new config for a rule definition. Note: many methods are required.
     */
    public static RuleDescriptorBuilder forDefinition(String languageId, RuleBehavior behavior) {
        return new RuleDefBuilder(behavior, languageId);
    }

    public <T> RuleDescriptorBuilder setProperty(PropertyDescriptor<T> descriptor, T value) {
        properties.setProperty(descriptor, value);
        return this;
    }

    public RuleDescriptorBuilder name(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public RuleDescriptorBuilder description(String description) {
        this.description = Objects.requireNonNull(description);
        return this;
    }

    public RuleDescriptorBuilder message(String message) {
        this.message = Objects.requireNonNull(message);
        return this;
    }

    public RuleDescriptorBuilder examples(List<String> examples) {
        this.examples = new ArrayList<>(Objects.requireNonNull(examples));
        return this;
    }

    public RuleDescriptorBuilder addExample(String example) {
        this.examples.add(Objects.requireNonNull(example));
        return this;
    }

    public abstract RuleDescriptor build();

}
