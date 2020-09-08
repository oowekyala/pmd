/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * Metadata about a rule. Custom rules implement {@link RuleBehavior}
 * instead of this interface. All this metadata is overridable in a rule
 * reference except the {@linkplain #getLanguageId() language ID} and the
 * {@linkplain #behavior() behavior}.
 *
 * <p>Rule descriptors are independent from {@link Language} instances,
 * so are independent from a particular analysis. In PMD 7, a {@link RuleSet}
 * would be a set of {@link RuleDescriptor} zipped with their properties,
 * which means the same ruleset could be reused in several analyses without
 * leaking state.
 *
 * <p>Under this scheme, a RuleReference is just a RuleDescriptor that
 * delegates the getBehavior method, and has fields for all the remaining
 * stuff.
 */
public interface RuleDescriptor {

    String getLanguageId();

    /** The implemented behavior of this rule. */
    RuleBehavior behavior();

    /**
     * The properties of this descriptor. You can set existing properties, but
     * not declare them. All the properties of the {@linkplain #behavior()}
     * are declared on it.
     */
    PropertySource properties();

    // Overridable metadata
    // Documentation has been stripped to remove clutter, this is basically
    // all of the Rule interface except:
    // - setters: we can use a builder pattern and make descriptors immutable.
    // This clarifies how rule reference behaves (they don't touch the state of
    // the reference rule, the only state is the RuleBehavior, which is not
    // configurable except upon initialization).
    // - min/max language version: this is really a property of the RuleBehavior
    // - lifecycle methods: they belong on RuleBehavior
    // - deepCopy: this is useless now
    // - PropertySource methods: RuleBehavior has #declaredProperties()

    String getName();

    boolean isDeprecated();

    String getSince();

    String getRuleSetName();

    String getMessage();

    String getDescription();

    List<String> getExamples();

    String getExternalInfoUrl();

    RulePriority getPriority();

}
