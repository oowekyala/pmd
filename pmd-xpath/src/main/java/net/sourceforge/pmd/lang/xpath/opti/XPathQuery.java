/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.Map;


/**
 * Wrapper for an XPath query.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface XPathQuery {

    /**
     * Returns a parsable string representing this expression.
     */
    String toParsableString();


    /**
     * Inlines the values of property descriptors.
     */
    void inlineProperties();


    /**
     * Retrieves the queries to be used by a rulechain pass.
     * Returns an empty map if the query cannot be split into
     * rulechain subqueries.
     */
    Map<NodeIdentifier, XPathQuery> getRulechainQueries();


}
