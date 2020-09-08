/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import net.sourceforge.pmd.properties.AbstractPropertySource;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 *
 */
final class RuleProperties extends AbstractPropertySource {

    RuleProperties(List<? extends PropertyDescriptor<?>> properties) {
        for (PropertyDescriptor<?> property : properties) {
            super.definePropertyDescriptor(property);
        }
    }

    @Override
    protected String getPropertySourceType() {
        return "rule";
    }

    @Override
    public String getName() {
        return "TODO remove this method";
    }

    @Override
    public void definePropertyDescriptor(PropertyDescriptor<?> propertyDescriptor) {
        throw new UnsupportedOperationException("Cannot define a new property");
    }
}
