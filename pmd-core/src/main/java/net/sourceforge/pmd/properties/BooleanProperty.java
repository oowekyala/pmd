/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties;

import static net.sourceforge.pmd.properties.ValueParserConstants.BOOLEAN_PARSER;

import net.sourceforge.pmd.properties.builders.PropertyDescriptorBuilderConversionWrapper;
import net.sourceforge.pmd.properties.builders.SingleValuePropertyBuilder;


/**
 * Defines a property type that supports single Boolean values.
 *
 * @author Brian Remedios
 * @version Refactored June 2017 (6.0.0)
 */
public final class BooleanProperty extends AbstractSingleValueProperty<Boolean> {

    /**
     * Constructor for BooleanProperty limited to a single value. Converts default argument string into a boolean.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param defaultBoolStr String representing the default value.
     * @param theUIOrder     UI order
     *
     * @deprecated will be removed in 7.0.0
     */
    public BooleanProperty(String theName, String theDescription, String defaultBoolStr, float theUIOrder) {
        this(theName, theDescription, Boolean.valueOf(defaultBoolStr), theUIOrder, false, true);
    }


    /** Master constructor. */ // Using a boxed type avoids NPEs caused by unboxing of a null default value
    private BooleanProperty(String theName, String theDescription, Boolean defaultValue, float theUIOrder, boolean isDefinedExternally, boolean hasDefaultValue) {
        super(theName, theDescription, defaultValue, theUIOrder, isDefinedExternally, hasDefaultValue);
    }


    /**
     * Constructor.
     *
     * @param theName        Name
     * @param theDescription Description
     * @param defaultValue   Default value
     * @param theUIOrder     UI order
     */
    public BooleanProperty(String theName, String theDescription, boolean defaultValue, float theUIOrder) {
        this(theName, theDescription, defaultValue, theUIOrder, false, true);
    }


    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }


    @Override
    public Boolean createFrom(String propertyString) throws IllegalArgumentException {
        return BOOLEAN_PARSER.valueOf(propertyString);
    }


    static PropertyDescriptorBuilderConversionWrapper.SingleValue<Boolean, BooleanPBuilder> extractor() {
        return new PropertyDescriptorBuilderConversionWrapper.SingleValue<Boolean, BooleanPBuilder>(Boolean.class, ValueParserConstants.BOOLEAN_PARSER) {
            @Override
            protected BooleanPBuilder newBuilder(String name) {
                return new BooleanPBuilder(name);
            }
        };
    }


    public static BooleanPBuilder named(String name) {
        return new BooleanPBuilder(name);
    }


    public static final class BooleanPBuilder extends SingleValuePropertyBuilder<Boolean, BooleanPBuilder> {
        private BooleanPBuilder(String name) {
            super(name);
        }


        @Override
        public BooleanProperty build() {
            return new BooleanProperty(this.name, this.description, this.defaultValue, this.uiOrder, this.isDefinedInXML, this.builderHasDefaultValue());
        }
    }

}
