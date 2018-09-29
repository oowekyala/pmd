/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.properties.StringProperty;


/**
 * Template for a language agnostic Saxon-only XPath rule.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public class SaxonXPathRule extends AbstractRule {

    public static final StringProperty XPATH_DESCRIPTOR
            = StringProperty.named("xpathExpression")
                            .desc("XPath expression")
                            .defaultValue("")
                            .uiOrder(1.0f)
                            .build();


    @Override
    public void apply(List<? extends Node> nodes, RuleContext ctx) {

    }


    private void initExpression() {
        XPathQuery query = new XPathOptimisationFacade().makeQuery(getProperty(XPATH_DESCRIPTOR), getPropertiesByPropertyDescriptor());
        query.optimise();
        // Jaxen compatibility layer
        String dump = query.toParsableString();




    }

}
