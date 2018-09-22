package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.lang.rule.XPathRule
import net.sourceforge.pmd.lang.xpath.ast.RulesetUtils.rsets

/**
 * Utilities to get rulesets or rules.
 */
private object RulesetUtils {

    /** List of category names. */
    val categories: List<String> = listOf("codestyle", "performance", "bestpractices", "security", "design", "documentation", "errorprone")

    // TODO for now we have a test dependency on PMD java
    // TODO make categories available from language module

    /** All rulesets (for Java). */
    val rsets by lazy {
        categories.map { "category/java/$it.xml" }.map { RuleSetFactory().createRuleSet(it) }
    }

}

/**
 * Execute some code on each XPath rule.
 *
 * @param consumer Thunk with the executed code. The parameters are the rule
 *                 and the xpath expression as a string.
 */
fun foreachXPathRule(consumer: (Rule, String) -> Unit) {
    rsets.flatMap { it.rules }
            .filter { it is XPathRule }
            .forEach {
                consumer(it, it.getProperty(XPathRule.XPATH_DESCRIPTOR))
            }
}
