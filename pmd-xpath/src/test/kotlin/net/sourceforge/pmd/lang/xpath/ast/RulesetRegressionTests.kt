package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import net.sf.saxon.sxpath.IndependentContext
import net.sf.saxon.sxpath.XPathEvaluator
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.lang.java.xpath.JavaFunctions
import net.sourceforge.pmd.lang.xpath.Initializer
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

                val (root, time, saxonTime) = {
                    var root: ASTXPathRoot? = null

                    val time = measureNanoTime {
                        root = parseXPathRoot(xpath)
                    }

                    val saxonTime = measureNanoTime {
                        val xpathEvaluator = XPathEvaluator()
                        val xpathStaticContext = xpathEvaluator.staticContext

                        rule.propertyDescriptors.forEach {
                            xpathStaticContext.declareVariable(null, it.name())
                        }

                        // Register PMD functions
                        Initializer.initialize(xpathStaticContext as IndependentContext)

                        xpathStaticContext.declareNamespace("pmd-java", "java:" + JavaFunctions::class.java.name)

                        xpathEvaluator.createExpression(xpath)
                    }

                    Triple(root!!, time, saxonTime)
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

                addTimingResult(TimingResult(time, saxonTime, numNodes, xpath.length, rule))
            }

        }

    }

    override fun listeners(): List<TestListener> = listOf(TimerListener)

    internal companion object {

        data class TimingResult(val timeNano: Long, val saxonTimeNano: Long, val numNodes: Int, val sourceLength: Int, val rule: Rule) {

            override fun toString(): String {
                return "$timeNano\t$saxonTimeNano\t$numNodes\t$sourceLength\t${rule.name}"

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

                results.sortBy { it.timeNano }

                val totalTime = toMillis(results.asSequence().map { it.timeNano }.sum())
                val totalSaxonTime = toMillis(results.asSequence().map { it.saxonTimeNano }.sum())

                val averageTime = totalTime.toDouble() / results.size
                val averageSaxonTime = totalSaxonTime.toDouble() / results.size

                val medianNano =
                        if (results.size % 2 == 0) (results[results.size / 2].timeNano + results[results.size / 2 - 1].timeNano).toDouble() / 2
                        else results[results.size / 2].timeNano.toDouble()

                val medianTime = medianNano * 1e-6

                println("Total time: $totalTime ms")
                println("Total time (saxon): $totalSaxonTime ms")
                println("Average time: $averageTime ms")
                println("Average time (saxon): $averageSaxonTime ms")
                println("Median time: $medianTime ms")
                println()

                results.sortedBy { it.rule.name }.forEach { println(it) }
            }

        }


    }

}
