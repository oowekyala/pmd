/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.builders;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.properties.PropertyDescriptor;


/**
 * Base class for property builders.
 *
 * @param <E> Value type of the built descriptor
 * @param <T> Concrete type of this builder instance. Removes code duplication at the expense of a few unchecked casts.
 *            Everything goes well if this parameter's value is correctly set.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public abstract class PropertyDescriptorBuilder<E, T extends PropertyDescriptorBuilder<E, T>> {

    protected String name;
    protected String description;
    protected float uiOrder = 0f;
    protected boolean isDefinedInXML = false;
    private boolean hasDefaultValue = true;


    protected PropertyDescriptorBuilder(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name must be provided");
        }
        this.name = name;
    }


    /**
     * Returns true if the default value is valid.
     * If this method returns false, this property
     * will have no default value.
     */
    protected boolean builderHasDefaultValue() {
        return hasDefaultValue;
    }
    

    /**
     * Specify the description of the property.
     *
     * @param desc The description
     *
     * @return The same builder
     */
    @SuppressWarnings("unchecked")
    public T desc(String desc) {
        if (StringUtils.isBlank(desc)) {
            throw new IllegalArgumentException("Description must be provided");
        }
        this.description = desc;
        return (T) this;
    }


    /**
     * Specify the UI order of the property.
     *
     * @param f The UI order
     *
     * @return The same builder
     */
    @SuppressWarnings("unchecked")
    public T uiOrder(float f) {
        this.uiOrder = f;
        return (T) this;
    }


    /**
     * Tags the property as required. Such a property
     * has no default value, and requires to be overridden
     * by the user in the XML, otherwise an exception
     * is thrown by the {@link net.sourceforge.pmd.rules.RuleFactory}.
     *
     * <p>Calling that method on the builder discards any
     * previously specified default values.
     *
     * @return The same builder
     */
    @SuppressWarnings("unchecked")
    public T isRequired() {
        hasDefaultValue = false;
        return (T) this;
    }

    /**
     * Builds the descriptor and returns it.
     *
     * @return The built descriptor
     * @throws IllegalArgumentException if parameters are incorrect
     */
    public abstract PropertyDescriptor<E> build();


    /**
     * Returns the name of the property to be built.
     */
    public String getName() {
        return name;
    }

}
