package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.RuleSet
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.lang.rule.XPathRule

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ParserRulesetRegressionTests : FunSpec({


    // TODO for now we have a test dependency on PMD java
    // TODO make categories available from language module


    val categories = listOf("codestyle", "performance", "bestpractices", "security", "design", "documentation", "errorprone")

    val rsets: List<RuleSet> = categories.map { "category/java/$it.xml" }.map { RuleSetFactory().createRuleSet(it) }



    rsets.flatMap { it.rules }
            .filter { it is XPathRule }
            .forEach {
                // Generate one test for each XPath rule
                parserTest("Test parsing rule ${it.name} (${it.ruleSetName})") {
                    val xpath = it.getProperty(XPathRule.XPATH_DESCRIPTOR)
                    try {
//                        parseXPathRoot(xpath)
                    } catch (e: ParseException) {
                        throw AssertionError("Parser failed, xpath is:\n\n$xpath\n\n", e)
                    }
                }
            }

})