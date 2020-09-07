/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.io.File;
import java.util.function.Predicate;

import net.sourceforge.pmd.internal.util.PredicateUtil;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.rule.RuleBehavior.DysfunctionalRuleException;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleAnalyser;
import net.sourceforge.pmd.lang.rule.RuleBehavior.RuleInitializationWarner;
import net.sourceforge.pmd.properties.AbstractPropertySource;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * A rule descriptor zipped with its properties. No properties can be
 * declared on this property source, properties may only be set.
 *
 * Todo this is to be the element type of a RuleSet
 */
public final class ConfiguredRuleDescriptor {

    private final RuleDescriptor descriptor;
    private final PropertySource properties;
    private Predicate<File> acceptsFile = PredicateUtil.always();

    public ConfiguredRuleDescriptor(RuleDescriptor descriptor) {
        this.descriptor = descriptor;
        properties = new RuleProperties(descriptor);
    }

    public PropertySource getProperties() {
        return properties;
    }

    public RuleDescriptor getRule() {
        return descriptor;
    }

    public RuleBehavior getBehavior() {
        return descriptor.getBehavior();
    }

    public void addFileFilter(Predicate<File> filter) {
        acceptsFile = acceptsFile.and(filter);
    }

    public boolean acceptsFile(File file) {
        return acceptsFile.test(file);
    }

    RuleAnalyser initialize(LanguageRegistry languageRegistry, RuleInitializationWarner warner) throws DysfunctionalRuleException {
        Language language = LanguageRegistry.findLanguageByTerseName(descriptor.getLanguageId());
        return descriptor.getBehavior().initialize(getProperties(), language, warner);
    }

    public ConfiguredRuleDescriptor newReference(BaseRuleDescriptor.RuleDescriptorConfig config) {
        ConfiguredRuleDescriptor newConfig = new ConfiguredRuleDescriptor(new RuleDescriptorReference(config, this.descriptor));
        for (PropertyDescriptor<?> prop : newConfig.getProperties().getPropertyDescriptors()) {
            // copy properties so far
            PropertySource.copyProperty(this.getProperties(), prop, newConfig.getProperties());
        }
        return newConfig;
    }

    private static final class RuleProperties extends AbstractPropertySource {

        private final RuleDescriptor descriptor;

        public RuleProperties(RuleDescriptor descriptor) {
            this.descriptor = descriptor;
            for (PropertyDescriptor<?> property : descriptor.getBehavior().declaredProperties()) {
                super.definePropertyDescriptor(property);
            }
        }

        @Override
        protected String getPropertySourceType() {
            return "rule";
        }

        @Override
        public String getName() {
            return descriptor.getName();
        }

        @Override
        public void definePropertyDescriptor(PropertyDescriptor<?> propertyDescriptor) {
            throw new UnsupportedOperationException("Cannot define a new property on rule " + descriptor.getName());
        }
    }
}
