/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.rule7.RuleImpl;
import net.sourceforge.pmd.rule7.RuleImpl.JvmRuleImpl;


public class JvmRuleBuilder extends RuleDescriptorBuilder {

    private Class<? extends JvmRuleImpl> implClass;


    public void setImplClass(Class<? extends JvmRuleImpl> clazz) {
        this.implClass = clazz;
        if (!RuleImpl.class.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException("Invalid implementation class " + implClass);
        }

        JvmRuleImpl first = newInstance();
        for (PropertyDescriptor<?> prop : first.getDeclaredProperties()) {
            properties.declareProperty(prop);
        }
    }

    Class<? extends JvmRuleImpl> getImplClass() {
        return implClass;
    }

    JvmRuleImpl newInstance() {
        try {
            return implClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Unable to build impl for rule " + name, e);
        }
    }


    @Override
    public BaseRuleDescriptor create() {
        return new JvmRuleDescriptor(this, newInstance());
    }
}
