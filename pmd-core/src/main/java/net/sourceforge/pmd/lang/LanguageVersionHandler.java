/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import net.sourceforge.pmd.lang.rule.impl.DefaultRuleViolationFactory;
import net.sourceforge.pmd.lang.rule.xpath.impl.XPathHandler;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.util.designerbindings.DesignerBindings;
import net.sourceforge.pmd.util.designerbindings.DesignerBindings.DefaultDesignerBindings;


/**
 * Interface for obtaining the classes necessary for checking source files of a
 * specific language.
 *
 * @author pieter_van_raemdonck - Application Engineers NV/SA - www.ae.be
 */
public interface LanguageVersionHandler {


    /**
     * Get the XPathHandler.
     */
    default XPathHandler getXPathHandler() {
        return XPathHandler.noFunctionDefinitions();
    }


    /**
     * @deprecated This is transitional
     */
    @Deprecated
    default void declareParserTaskProperties(PropertySource source) {
        // do nothing
    }


    /**
     * Get the Parser.
     *
     * @return Parser
     */
    Parser getParser();



    /**
     * Get the RuleViolationFactory.
     */
    default RuleViolationFactory getRuleViolationFactory() {
        return DefaultRuleViolationFactory.defaultInstance();
    }


    /**
     * Returns the metrics provider for this language version,
     * or null if it has none.
     *
     * Note: this is experimental, ie unstable until 7.0.0, after
     * which it will probably be promoted to a stable API. For
     * instance the return type will probably be changed to an Optional.
     */
    @Experimental
    default LanguageMetricsProvider getLanguageMetricsProvider() {
        return null;
    }


    /**
     * Returns the designer bindings for this language version.
     * Null is not an acceptable result, use {@link DefaultDesignerBindings#getInstance()}
     * instead.
     *
     * @since 6.20.0
     */
    @Experimental
    default DesignerBindings getDesignerBindings() {
        return DefaultDesignerBindings.getInstance();
    }

}
