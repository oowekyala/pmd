/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.properties.AbstractPropertySource;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.lang.rule.RuleBehavior.DysfunctionalRuleException;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleInitializationWarner;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleAnalyser;

/**
 * A rule descriptor zipped with its properties. No properties can be
 * declared on this property source, properties may only be set.
 *
 * Todo this is to be the element type of a RuleSet
 */
public final class ConfiguredRuleDescriptor extends AbstractPropertySource {

    private final RuleDescriptor descriptor;

    public ConfiguredRuleDescriptor(RuleDescriptor descriptor) {
        this.descriptor = descriptor;
        for (PropertyDescriptor<?> property : descriptor.getBehavior().declaredProperties()) {
            super.definePropertyDescriptor(property);
        }
    }

    public RuleAnalyser initialize(Language language, RuleInitializationWarner warner) throws DysfunctionalRuleException {
        return descriptor.getBehavior().initialize(this, language, warner);
    }

    /**
     * @deprecated This will throw
     */
    @Override
    @Deprecated
    public void definePropertyDescriptor(PropertyDescriptor<?> propertyDescriptor) {
        throw new UnsupportedOperationException("Cannot define a new property on rule " + descriptor.getName());
    }

    @Override
    protected String getPropertySourceType() {
        return "rule";
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }
}
