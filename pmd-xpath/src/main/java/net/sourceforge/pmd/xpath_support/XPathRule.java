/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.xpath_support;

import net.sourceforge.pmd.Rule;


/**
 * @author Clément Fournier
 * @since 7.0.0
 */
public interface XPathRule extends Rule {


    String getXPathExpression();


}
