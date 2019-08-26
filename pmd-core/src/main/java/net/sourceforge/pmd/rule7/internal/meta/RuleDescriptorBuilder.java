/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;

import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.rule7.RuleDescriptor.DeprecationInfo;
import net.sourceforge.pmd.rule7.RuleDescriptor.LanguageVersionRange;
import net.sourceforge.pmd.rule7.RuleDescriptor.RuleExample;

public abstract class RuleDescriptorBuilder {

    final PropertyBundle properties = new PropertyBundleImpl();
    LanguageVersionRange languageVersionRange;
    DeprecationInfo deprecation;
    String name;
    String since;
    String message;
    String description;
    List<RuleExample> examples;
    RulePriority priority;

    public RuleDescriptorBuilder setLanguageVersionRange(LanguageVersionRange languageVersionRange) {
        this.languageVersionRange = languageVersionRange;
        return this;
    }

    public RuleDescriptorBuilder setDeprecation(DeprecationInfo deprecation) {
        this.deprecation = deprecation;
        return this;
    }

    public RuleDescriptorBuilder declareProperty(PropertyDescriptor<?> descriptor) {
        this.properties.declareProperty(descriptor);
        return this;
    }

    public <T> RuleDescriptorBuilder setProperty(PropertyDescriptor<T> descriptor, T value) {
        this.properties.setProperty(descriptor, value);
        return this;
    }

    public RuleDescriptorBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public RuleDescriptorBuilder setSince(String since) {
        this.since = since;
        return this;
    }

    public RuleDescriptorBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public RuleDescriptorBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public RuleDescriptorBuilder setExamples(List<RuleExample> examples) {
        this.examples = examples;
        return this;
    }

    public RuleDescriptorBuilder setPriority(RulePriority priority) {
        this.priority = priority;
        return this;
    }

    public abstract BaseRuleDescriptor create();
}
