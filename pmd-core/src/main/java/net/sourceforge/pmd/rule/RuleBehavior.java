/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.AstVisitor;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.GlobalAnalysisListener;


/**
 * The configurable behavior of a rule. This is the interface rule classes
 * should implement. A {@link RuleBehavior} is not directly the behavior,
 * it's some additional metadata and initialization routine for the actual
 * runnable rule, which is represented by a {@link RuleAnalyser}.
 *
 * <p>The lifecycle of an analysis looks like so:
 * <ul>
 * <li>Rule descriptors are collected from rulesets by following references.
 * Their {@link RuleBehavior} is used to collect properties. The output of this
 * phase is the final set of rules that comprise the ruleset, and their corresponding
 * property bundles. No {@link #initialize(PropertySource, Language, RuleInitializationWarner) initialize}
 * method has been called yet.
 * <li>For each rule descriptor of this set, we call {@link #initialize(PropertySource, Language, RuleInitializationWarner) initialize}
 * with the corresponding property bundle, and the language instance the
 * analysis will be using. Exceptions are reported, in particular, {@link DysfunctionalRuleException}s.
 * The output of this phase is a set of {@link RuleAnalyser}s, which are
 * all functional.
 * <li>During analysis, each {@link RuleAnalyser} is reset with {@link RuleAnalyser#nextFile() FileAnalyser::nextFile}
 * before being fed with nodes that match the {@linkplain #getTargetSelector() target selector}.
 * {@link RuleAnalyser#apply(Node, RuleContext) FileAnalyser::apply}
 * performs side-effects on the {@link RuleContext} to report events like
 * violations. Where those events end up is the problem of the {@link FileAnalysisListener},
 * which most commonly forwards to a {@link Renderer}.
 * <li>When there are no more files to process, {@link RuleAnalyser#endAnalysis() endAnalysis}
 * is called on each file analyser. The {@link GlobalAnalysisListener}
 * is closed, which flushes the report, and caches violations if needed.
 * {@link Language} instances are closed. The analysis is terminated.
 * </ul>
 */
public interface RuleBehavior {

    /**
     * The target selector for this rule. This must be constant throughout
     * the analysis (this will be only called once, around the time {@link #initialize(PropertySource, Language,
     * RuleInitializationWarner) initialize}
     * is called), so is not part of the {@link RuleAnalyser} interface.
     */
    default RuleTargetSelector getTargetSelector() {
        return RuleTargetSelector.forRootOnly();
    }

    /**
     * Returns the properties that this rule accepts. These will be
     * declared on a {@link PropertySource}, set to the values provided
     * in the ruleset XML, before they're passed to {@link #initialize(PropertySource, Language, RuleInitializationWarner)}.
     */
    default List<? extends PropertyDescriptor<?>> declaredProperties() {
        return Collections.emptyList();
    }

    /**
     * Produce a new file analyser, given some fully-initialized properties.
     *
     * <p>This is called once per thread, so either your file analyser
     * implementation is thread-safe, and you can return the same instance
     * always, or it's not, and you can return a new instance always.
     *
     * <p>If the rule is misconfigured, the warner parameter should be used
     * to report it. If the configuration can still provide a useful rule,
     * then only {@link RuleInitializationWarner#configWarning(String, Object...) configWarning}s
     * should be reported. If the configuration is so broken that the
     * rule would be useless, then {@link RuleInitializationWarner#fatalConfigError(String, Object...)}
     * should be used to produce an exception that will be thrown.
     *
     * @param properties Property bundle, on which all properties mentioned
     *                   by {@link #declaredProperties()} are declared
     * @param language   Language instance global to the analysis
     * @param warner     An object to report misconfigurations that property
     *                   constraints couldn't catch.
     *
     * @return A file analyser
     *
     * @throws DysfunctionalRuleException If the configuration is so broken,
     *                                    that no useful file analyser can be
     *                                    produced.
     * @throws RuntimeException           This should be caught by calling code and
     *                                    reported the same way as a {@link DysfunctionalRuleException}
     */
    RuleAnalyser initialize(PropertySource properties, Language language, RuleInitializationWarner warner) throws DysfunctionalRuleException;


    /**
     * Tests whether the rule applies to the given language version.
     * This is used to ignore some files.
     */
    default boolean appliesToVersion(LanguageVersion version) {
        // TODO make a LanguageVersionRange class
        // TODO validate that version is for the proper language
        return true;
    }

    /**
     * Thrown by {@link #initialize(PropertySource, Language, RuleInitializationWarner)}
     * if the configuration is so broken, that no useful file analyser
     * can be produced.
     */
    class DysfunctionalRuleException extends Exception {

        DysfunctionalRuleException(String reason) {
            super(reason);
        }

    }

    /**
     * Reporter for {@link #initialize(PropertySource, Language, RuleInitializationWarner)}.
     */
    interface RuleInitializationWarner {

        /**
         * Report a misconfiguration that doesn't make the rule completely
         * useless. Maybe report usage of deprecated properties, though ideally
         * the property framework could handle that itself.
         *
         * @param message Message templated by a {@link MessageFormat}
         * @param args    Format arguments
         */
        void configWarning(String message, Object... args);

        /**
         * Report a misconfiguration that is unrecoverable. The rule
         * cannot be executed, for example because the properties make
         * the rule a noop.
         *
         * @param message Message templated by a {@link MessageFormat}
         * @param args    Format arguments
         *
         * @return Never. The return value is just to allow the caller
         *     to pretend it's throwing the exception, so that the call
         *     to this method may be identified as an exit point by the Java compiler.
         *
         * @throws DysfunctionalRuleException Always.
         */
        DysfunctionalRuleException fatalConfigError(String message, Object... args) throws DysfunctionalRuleException;


    }


    /**
     * The reporting behavior of a rule. This is fully initialized and
     * will receive nodes to report them on the rule context.
     */
    interface RuleAnalyser {

        // copy doc from Rule#apply: this is fed with nodes that match the RuleTargetSelector
        void apply(Node node, RuleContext ctx);

        /**
         * Signals the analysis of a new file is about to start.
         */
        default void nextFile() {
            // by default do nothing
        }

        /** Called when no more files will be processed. */
        default void endAnalysis() {
            // by default do nothing
        }
    }


    class VisitorAnalyser implements RuleAnalyser {

        private final AstVisitor<RuleContext, Void> visitor;

        public VisitorAnalyser(AstVisitor<RuleContext, Void> visitor) {
            this.visitor = visitor;
        }

        @Override
        public final void apply(Node node, RuleContext ctx) {
            node.acceptVisitor(visitor, ctx);
        }
    }
}
