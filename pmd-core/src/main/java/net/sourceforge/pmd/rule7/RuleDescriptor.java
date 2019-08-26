/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.rule7.internal.meta.LangVersionRangeImpl;
import net.sourceforge.pmd.rule7.internal.meta.PropertyBundle;

/**
 * A rule descriptor describes the overridable metadata of a rule, including
 * its {@link #getProperties() properties} It can provide instances of
 * {@link RuleImpl}, which execute the rule.
 */
public interface RuleDescriptor {


    LanguageVersionRange getLanguageVersionRange();


    @Nullable
    DeprecationInfo getDeprecation();


    /** Returns the properties configured on this rule. */
    PropertyBundle getProperties();


    /**
     * Spawn a new visitor. The same visitor instance may be used if eg,
     * it is stateless.
     */
    RuleImpl getVisitor();


    String getName();


    String getSince();


    String getMessage();


    String getDescription();


    List<RuleExample> getExamples();


    RulePriority getPriority();


    interface DeprecationInfo {

        String getSince();


        @Nullable
        RuleDescriptor getReplacement();

    }

    interface RuleExample {

        interface StringExample extends RuleExample {

            String getText();


            String getExplanation();
        }

        interface ExampleConfig extends RuleExample {


            RuleDescriptor configured();
        }
    }


    interface LanguageVersionRange {


        Language getLanguage();


        @Nullable
        LanguageVersion getMin();


        @Nullable
        LanguageVersion getMax();


        LanguageVersionRange withMin(@Nullable LanguageVersion ver);


        LanguageVersionRange withMax(@Nullable LanguageVersion ver);


        /** Returns true if this range accepts the given language version. */
        boolean contains(LanguageVersion version);


        /** Returns a range encompassing all the versions of the given language. */
        static LanguageVersionRange allOf(Language lang) {
            return new LangVersionRangeImpl(lang, null, null);
        }
    }

}
