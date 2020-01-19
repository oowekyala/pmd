/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import net.sourceforge.pmd.lang.xpath.ast.XPathSideEffectingVisitor;


/**
 *
 */
public interface PmdXPathRule extends Rule, XPathSideEffectingVisitor<RuleContext>, ImmutableLanguage {

}
