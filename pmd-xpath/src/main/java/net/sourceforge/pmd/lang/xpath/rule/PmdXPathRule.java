/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import net.sourceforge.pmd.lang.xpath.ast.SideEffectingVisitor;
import net.sourceforge.pmd.lang.xpath.ast.XPathGenericVisitor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface PmdXPathRule extends Rule, SideEffectingVisitor<RuleContext>, ImmutableLanguage {

}
