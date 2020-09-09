/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * Configuration metadata about a rule. Custom rules implement {@link RuleBehavior}
 * instead of this interface. All this metadata is overridable in a rule
 * reference except the {@linkplain #getLanguageId() language ID} and the
 * {@linkplain #behavior() behavior}. See {@link RuleDescriptorBuilder#forReference(RuleDescriptor)}.
 *
 * <p>Rule descriptors are independent from {@link Language} instances,
 * so are independent from a particular analysis. In PMD 7, a {@link RuleSet}
 * would be a set of {@link RuleDescriptor}, which means the same ruleset
 * could be reused in several analyses without leaking state.
 */
public interface RuleDescriptor {

    String getLanguageId();

    /** The implemented behavior of this rule. */
    RuleBehavior behavior();

    // Documentation has been stripped to remove clutter, this is basically
    // all of the Rule interface except:
    // - setters: we can use a builder pattern and make descriptors immutable.
    // This clarifies how rule reference behaves (they don't touch the state of
    // the reference rule, the only state is the RuleBehavior, which is not
    // configurable except upon initialization).
    // - min/max language version: this is really a property of the RuleBehavior
    // - lifecycle methods: they belong on RuleBehavior
    // - deepCopy: this is useless now
    // - PropertySource methods: only a couple of read-only methods are kept


    /**
     * Returns the value of the given property
     *
     * @param propertyDescriptor Descriptor of the property
     * @param <T>                Type of values
     *
     * @return The value set for this property
     *
     * @throws IllegalArgumentException If the property is not declared by the {@link #behavior()}
     */
    <T> T getProperty(PropertyDescriptor<T> propertyDescriptor);


    PropertyDescriptor<?> getPropertyDescriptor(String name);


    default List<PropertyDescriptor<?>> getPropertyDescriptors() {
        return Collections.unmodifiableList(behavior().declaredProperties());
    }

    // Overridable metadata

    String getName();

    String getRuleSetName();

    String getMessage();

    RulePriority getPriority();

    // all the following is unimportant metadata, used only for documentation
    // it would be nice to put it into a DataMap, which would allow for implementing #2315
    String getExternalInfoUrl();

    String getDescription();

    List<String> getExamples();

    boolean isDeprecated();

    String getSince();
}
