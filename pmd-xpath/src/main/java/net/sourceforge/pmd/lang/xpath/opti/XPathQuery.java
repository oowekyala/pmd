/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.Map;


/**
 * Wrapper for an XPath query.
 */
public interface XPathQuery {


    /**
     * Retrieves the queries to be used by a rulechain pass.
     * Returns an empty map if the query cannot be split into
     * rulechain subqueries.
     */
    Map<String, String> getRulechainQueries();


}
