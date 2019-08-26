/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;

import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * @author Clément Fournier
 */
public interface PropertyBundle {

    void declareProperty(PropertyDescriptor<?> propertyDescriptor);


    PropertyDescriptor<?> getPropertyDescriptor(String name);


    List<PropertyDescriptor<?>> getOverriddenPropertyDescriptors();


    List<PropertyDescriptor<?>> getPropertyDescriptors();


    <T> T getProperty(PropertyDescriptor<T> propertyDescriptor);


    boolean isPropertyOverridden(PropertyDescriptor<?> propertyDescriptor);


    <T> void setProperty(PropertyDescriptor<T> propertyDescriptor, T value);


    Map<PropertyDescriptor<?>, Object> getOverriddenValues();


    Map<PropertyDescriptor<?>, Object> getAllValues();
}
