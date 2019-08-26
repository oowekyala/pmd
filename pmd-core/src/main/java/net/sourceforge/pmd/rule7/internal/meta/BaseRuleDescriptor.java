/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.rule7.internal.meta;


import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.rule7.RuleDescriptor;

public abstract class BaseRuleDescriptor implements RuleDescriptor {


    private final LanguageVersionRange languageVersionRange;
    private final @Nullable DeprecationInfo deprecation;
    private final PropertyBundle properties;
    private final String name;
    private final String since;
    private final String message;
    private final String description;
    private final List<RuleExample> examples;
    private final RulePriority priority;

    protected BaseRuleDescriptor(RuleDescriptorBuilder builder) {
        this.languageVersionRange = builder.languageVersionRange;
        this.deprecation = builder.deprecation;
        this.properties = builder.properties;
        this.name = builder.name;
        this.since = builder.since;
        this.message = builder.message;
        this.description = builder.description;
        this.examples = new ArrayList<>(builder.examples);
        this.priority = builder.priority;
    }

    @Override
    public LanguageVersionRange getLanguageVersionRange() {
        return languageVersionRange;
    }

    @Nullable
    @Override
    public DeprecationInfo getDeprecation() {
        return deprecation;
    }

    @Override
    public PropertyBundle getProperties() {
        return properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSince() {
        return since;
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
    public List<RuleExample> getExamples() {
        return examples;
    }

    @Override
    public RulePriority getPriority() {
        return priority;
    }
}
