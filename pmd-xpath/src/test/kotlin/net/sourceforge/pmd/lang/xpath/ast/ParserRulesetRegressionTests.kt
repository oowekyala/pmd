package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleSet
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.lang.rule.XPathRule
import kotlin.system.measureNanoTime

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ParserRulesetRegressionTests : FunSpec() {

    init {

        // TODO for now we have a test dependency on PMD java
        // TODO make categories available from language module


        val categories = listOf("codestyle", "performance", "bestpractices", "security", "design", "documentation", "errorprone")

        val rsets: List<RuleSet> = categories.map { "category/java/$it.xml" }.map { RuleSetFactory().createRuleSet(it) }



        rsets.flatMap { it.rules }
                .filter { it is XPathRule }
                .forEach {
                    val rule = it

                    // Generate one test for each XPath rule
                    parserTest("Test parsing rule ${it.name} (${it.ruleSetName})") {
                        val xpath = it.getProperty(XPathRule.XPATH_DESCRIPTOR)
                        val (root, time) = try {
                            var root: ASTXPathRoot? = null

                            val time = measureNanoTime {
                                root = parseXPathRoot(xpath)
                            }

                            Pair(root!!, time)
                        } catch (e: ParseException) {
                            throw AssertionError("Parser failed, xpath is:\n\n$xpath\n\n", e)
                        }

                        var numNodes = 0
                        root.jjtAccept(object : XPathParserVisitorAdapter<Any?>() {
                            override fun visit(node: XPathNode, data: Any?): Any? {
                                numNodes += 1
                                return super.visit(node, data)
                            }
                        }, null)

                        addTimingResult(TimingResult(time, numNodes, xpath.length, rule))
                    }
                }
    }

    override fun listeners(): List<TestListener> = listOf(TimerListener)


    internal companion object {

        // a bit useless
        data class TimingResult(val time: Long, val numNodes: Int, val sourceLength: Int, val rule: Rule) {
            override fun toString(): String {
                return "$time\t$numNodes\t$sourceLength\t${rule.name}"
            }
        }


        private val results = mutableListOf<TimingResult>()


        fun addTimingResult(result: TimingResult) {
            results += result
        }


        object TimerListener : TestListener {

            override fun afterSpec(description: Description, spec: Spec) {

                val totalTime = results.map { it.time }.sum()
                val averageTime = totalTime.toDouble() / results.size

                println("Total time: $totalTime")
                println("Average time: $averageTime")

                results.sortedBy { it.rule.name }.forEach { println(it) }
            }
        }

    }


}


