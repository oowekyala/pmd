package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleSet
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.lang.ast.TokenMgrError
import net.sourceforge.pmd.lang.rule.XPathRule
import java.time.Duration
import kotlin.system.measureNanoTime

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class RulesetRegressionTests : FunSpec() {
    init {

        // TODO for now we have a test dependency on PMD java
        // TODO make categories available from language module


        val categories = listOf("codestyle", "performance", "bestpractices", "security", "design", "documentation", "errorprone")

        val rsets: List<RuleSet> = categories.map { "category/java/$it.xml" }.map { RuleSetFactory().createRuleSet(it) }



        rsets.flatMap { it.rules }
                .filter { it is XPathRule }
                .forEach { rule ->
                    // Generate one test for each XPath rule
                    parserTest("Test parsing rule ${rule.name} (${rule.ruleSetName})") {
                        val xpath = rule.getProperty(XPathRule.XPATH_DESCRIPTOR)

                        val (root, time) = try {

                            var root: ASTXPathRoot? = null

                            val time = measureNanoTime {
                                root = parseXPathRoot(xpath)

                            }
                            Pair(root!!, time)

                        } catch (e: Exception) {
                            val ex = when (e) {
                                is ParseException -> e
                                is TokenMgrError -> e
                                else -> throw e
                            }
                            throw AssertionError("Parser failed, xpath is:\n\n$xpath\n\n", ex)
                        }


                        var numNodes = 0
                        root.jjtAccept(object : AbstractXPathGenericVisitor<Any?>() {

                            override fun defaultVisit(node: XPathNode?, data: Any?): Any? {
                                numNodes += 1
                                return super.defaultVisit(node, data)
                            }
                        }, null)

                        addTimingResult(TimingResult(time, numNodes, xpath.length, rule))
                    }
                }
    }

    override fun listeners(): List<TestListener> = listOf(TimerListener)

    internal companion object {

        data class TimingResult(val time: Long, val numNodes: Int, val sourceLength: Int, val rule: Rule) {

            override fun toString(): String {
                return "$time\t$numNodes\t$sourceLength\t${rule.name}"

            }

        }

        private val results = mutableListOf<TimingResult>()


        fun addTimingResult(result: TimingResult) {
            results += result

        }

        fun toMillis(nanos: Long) = Duration.ofNanos(nanos).toMillis()


        object TimerListener : TestListener {

            // This is for eg spreadsheet plotting
            override fun afterSpec(description: Description, spec: Spec) {

                results.sortBy { it.time }

                val totalTime = results.map { it.time }.sum().let { toMillis(it) }

                val averageTime = totalTime.toDouble() / results.size

                val medianNano =
                        if (results.size % 2 == 0) (results[results.size / 2].time + results[results.size / 2 - 1].time).toDouble() / 2
                        else results[results.size / 2].time.toDouble()

                val medianTime = medianNano * 1e-6

                println("Total time: $totalTime ms")
                println("Average time: $averageTime ms")
                println("Median time: $medianTime ms")

                results.sortedBy { it.rule.name }.forEach { println(it) }
            }

        }


    }

}
