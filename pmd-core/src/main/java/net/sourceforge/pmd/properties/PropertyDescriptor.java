/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import java.util.Map;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSetWriter;


/**
 * Property value descriptor that defines the use and requirements for setting
 * property values for use within PMD and any associated GUIs. Concrete descriptor
 * instances are immutable, and provide validation and serialization utilities
 * specific to the datatype the handle.
 *
 * <p>This interface is primarily specialized according to whether the property is
 * multi-valued or single-valued, see {@link SingleValuePropertyDescriptor} and
 * {@link MultiValuePropertyDescriptor}.
 *
 * <p>Several interfaces further specialize the behaviour of descriptors to
 * accommodate specific types of descriptors, see {@link NumericPropertyDescriptor}
 * and {@link EnumeratedPropertyDescriptor}.
 *
 * @param <T> type of the property's value. This is a list type for multi-valued properties.
 *
 * @author Brian Remedios
 * @author Clément Fournier
 * @version Refactored June 2017 (6.0.0)
 */
public interface PropertyDescriptor<T> extends Comparable<PropertyDescriptor<?>> {

    /**
     * The name of the property. Properties
     *
     * @return String
     */
    String name();


    /**
     * Describes the property and the role it plays within the rule it is specified for.
     * Could be used in a tooltip.
     *
     * @return A string description
     */
    String description();


    /**
     * Denotes the value datatype. For {@linkplain #isMultiValue() multi-valued properties}, this
     * is not the List class but the list's component class.
     *
     * @return Class of the value type
     */
    Class<?> type();


    /**
     * Returns whether the property is multi-valued. In that case,
     * the value of the property is a parametrization of {@link java.util.List},
     * and multiple values can be specified in the ruleset XML by
     * delimiting them with a delimiter specific to this property.
     * If this method returns true, this object can be safely
     * downcasted to {@link MultiValuePropertyDescriptor}.
     *
     * @return Whether this property is multi-valued or not
     */
    boolean isMultiValue();


    /**
     * Default value to use when the user hasn't specified one or
     * when they wish to revert to a known-good state. Some properties
     * have no default value (see {@link #hasNoDefaultValue()}), in
     * which case the return value of this method cannot be given a
     * meaningful interpretation.
     *
     * @return The default value
     */
    T defaultValue();


    /**
     * Returns true if this property descriptor has no default value. In that case,
     * the property is <i>required</i> to be given a value in the ruleset XML, otherwise
     * the {@link net.sourceforge.pmd.rules.RuleFactory} throws a construction exception.
     *
     * <p>If this method returns true, {@link #defaultValue()} cannot be given any meaningful
     * interpretation.
     *
     * @return {@code true} if this descriptor has no default value.
     */
    boolean hasNoDefaultValue();


    /**
     * Validation function that returns a diagnostic error message for a sample property value.
     * Returns null if the value is acceptable.
     *
     * @param value The value to check.
     *
     * @return A diagnostic message.
     */
    String errorFor(T value);


    /**
     * Denotes the relative order the property field should occupy if we are using an auto-generated UI to display and
     * edit property values. If the value returned has a non-zero fractional part then this is can be used to place
     * adjacent fields on the same row.
     *
     * <p>Example:<br> name -&gt; 0.0 description 1.0 minValue -&gt; 2.0 maxValue -&gt; 2.1 </p> ..would have their
     * fields placed like:<br>
     *
     * {@code name: [ ] description: [ ] minimum: [ ] maximum: [ ]}
     *
     * @return float
     */
    float uiOrder();


    /**
     * Returns the value represented by this string.
     *
     * @param propertyString The string to parse
     *
     * @return The value represented by the string
     * @throws IllegalArgumentException if the given string cannot be parsed
     */
    T valueFrom(String propertyString) throws IllegalArgumentException;


    /**
     * Formats the object onto a string suitable for storage within the property map.
     *
     * @param value Object
     *
     * @return String
     */
    String asDelimitedString(T value);


    /**
     * A convenience method that returns an error string if the rule holds onto a property value that has a problem.
     * Returns null otherwise.
     *
     * @param rule Rule
     *
     * @return String
     */
    String propertyErrorFor(Rule rule);


    /**
     * If the datatype is a String then return the preferred number of rows to allocate in the text widget, returns a
     * value of one for all other types. Useful for multi-line XPATH editors.
     *
     * @return int
     */
    int preferredRowCount();


    /**
     * Returns a map representing all the property attributes of the receiver in string form.
     *
     * @return map
     */
    Map<PropertyDescriptorField, String> attributeValuesById();


    /**
     * True if this descriptor was defined in the ruleset xml. This precision is necessary for the {@link RuleSetWriter}
     * to write out the property correctly: if it was defined externally, then its definition must be written out,
     * otherwise only its value.
     *
     * @return True if the descriptor was defined in xml
     */
    boolean isDefinedExternally();


    /**
     * Returns true if the given object is a property descriptor
     * with the same name as this one.
     *
     * @param o Object to compare
     *
     * @return True if these objects are considered equal w.r.t. their name
     */
    @Override
    boolean equals(Object o);

}
