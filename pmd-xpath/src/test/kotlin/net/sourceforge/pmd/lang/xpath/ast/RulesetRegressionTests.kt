package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.Rule
import java.time.Duration
import kotlin.system.measureNanoTime

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class RulesetRegressionTests : FunSpec() {
    init {
        foreachXPathRule { rule, xpath ->

            // Generate one test for each XPath rule
            parserTest("Test parsing rule ${rule.name} (${rule.ruleSetName})") {

                val (root, time) = {
                    var root: ASTXPathRoot? = null

                    val time = measureNanoTime {
                        root = parseXPathRoot(xpath)

                    }
                    Pair(root!!, time)
                } catchAnyParserError {
                    throw AssertionError("Parser failed, xpath is:\n\n$xpath\n\n", it)
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
