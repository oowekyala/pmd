/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.properties.PropertyDescriptor;

public class PropertyBundleImpl implements PropertyBundle {

    /** The list of known properties that can be configured. */
    private final List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();

    /**
     * The values for each property that were overridden here.
     * Default property values are not contained in this map.
     * In other words, if this map doesn't contain a descriptor
     * which is in {@link #propertyDescriptors}, then it's assumed
     * to have a default value.
     */
    private final Map<PropertyDescriptor<?>, Object> propertyValuesByDescriptor = new HashMap<>();

    public PropertyBundleImpl() {}

    public PropertyBundleImpl(PropertyBundle other) {
        this.propertyDescriptors.addAll(other.getPropertyDescriptors());
        this.propertyValuesByDescriptor.putAll(other.getOverriddenValues());
    }

    @Override
    public void declareProperty(PropertyDescriptor<?> propertyDescriptor) {
        // Check to ensure the property does not already exist.
        if (getPropertyDescriptor(propertyDescriptor.name()) != null) {
            throw new IllegalArgumentException("There is already a PropertyDescriptor with name '"
                                                   + propertyDescriptor.name() + "' defined.");

        }
        propertyDescriptors.add(propertyDescriptor);
        // Sort in UI order
        Collections.sort(propertyDescriptors);
    }


    @Override
    public PropertyDescriptor<?> getPropertyDescriptor(String name) {
        for (PropertyDescriptor<?> propertyDescriptor : propertyDescriptors) {
            if (name.equals(propertyDescriptor.name())) {
                return propertyDescriptor;
            }
        }
        return null;
    }


    @Override
    public final List<PropertyDescriptor<?>> getOverriddenPropertyDescriptors() {
        return new ArrayList<>(propertyValuesByDescriptor.keySet());
    }


    @Override
    public List<PropertyDescriptor<?>> getPropertyDescriptors() {
        return Collections.unmodifiableList(propertyDescriptors);
    }


    @Override
    public <T> T getProperty(PropertyDescriptor<T> propertyDescriptor) {
        checkValidPropertyDescriptor(propertyDescriptor);
        T result = propertyDescriptor.defaultValue();
        if (propertyValuesByDescriptor.containsKey(propertyDescriptor)) {
            @SuppressWarnings("unchecked")
            T value = (T) propertyValuesByDescriptor.get(propertyDescriptor);
            result = value;
        }
        return result;
    }


    @Override
    public boolean isPropertyOverridden(PropertyDescriptor<?> propertyDescriptor) {
        return propertyValuesByDescriptor.containsKey(propertyDescriptor);
    }


    @Override
    public <T> void setProperty(PropertyDescriptor<T> propertyDescriptor, T value) {
        checkValidPropertyDescriptor(propertyDescriptor);
        if (value instanceof List) {
            propertyValuesByDescriptor.put(propertyDescriptor, Collections.unmodifiableList((List) value));
        } else {
            propertyValuesByDescriptor.put(propertyDescriptor, value);
        }
    }


    /**
     * Checks whether this property descriptor is defined for this property source.
     *
     * @param propertyDescriptor The property descriptor to check
     */
    private void checkValidPropertyDescriptor(PropertyDescriptor<?> propertyDescriptor) {
        if (!hasDescriptor(propertyDescriptor)) {
            throw new IllegalArgumentException("Undefined property descriptor: "
                                                   + propertyDescriptor);
        }
    }

    private boolean hasDescriptor(PropertyDescriptor<?> propertyDescriptor) {
        return propertyDescriptors.contains(propertyDescriptor);
    }


    @Override
    public final Map<PropertyDescriptor<?>, Object> getOverriddenValues() {
        return new HashMap<>(propertyValuesByDescriptor);
    }


    @Override
    public Map<PropertyDescriptor<?>, Object> getAllValues() {
        if (propertyDescriptors.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<PropertyDescriptor<?>, Object> propertiesByPropertyDescriptor = new HashMap<>(propertyDescriptors.size());
        // Fill with existing explicitly values
        propertiesByPropertyDescriptor.putAll(this.propertyValuesByDescriptor);

        // Add default values for anything not yet set
        for (PropertyDescriptor<?> propertyDescriptor : this.propertyDescriptors) {
            if (!propertiesByPropertyDescriptor.containsKey(propertyDescriptor)) {
                propertiesByPropertyDescriptor.put(propertyDescriptor, propertyDescriptor.defaultValue());
            }
        }

        return Collections.unmodifiableMap(propertiesByPropertyDescriptor);
    }


    public String dysfunctionReason() {
        for (PropertyDescriptor<?> descriptor : getOverriddenPropertyDescriptors()) {
            String error = errorForPropCapture(descriptor);
            if (error != null) {
                return error;
            }
        }
        return null;
    }


    private <T> String errorForPropCapture(PropertyDescriptor<T> descriptor) {
        return descriptor.errorFor(getProperty(descriptor));
    }
}
