package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import net.sf.saxon.sxpath.IndependentContext
import net.sf.saxon.sxpath.XPathEvaluator
import net.sf.saxon.trans.XPathException
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.lang.java.xpath.JavaFunctions
import net.sourceforge.pmd.lang.xpath.Initializer
import org.apache.commons.lang3.StringUtils.center
import java.time.Duration
import kotlin.system.measureNanoTime

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class RulesetRegressionTests : XPathParserTestSpec(){
    init {
        foreachXPathRule { rule, xpath ->

            // Generate one test for each XPath rule
            parserTest("Test parsing rule ${rule.name} (${rule.ruleSetName})") {

                val (root, time, saxonTime) = {
                    var root: ASTXPathRoot? = null

                    val time = measureNanoTime {
                        root = parseXPathRoot(xpath)
                    }

                    val xpathEvaluator = XPathEvaluator()
                    val xpathStaticContext = xpathEvaluator.staticContext

                    rule.propertyDescriptors.forEach {
                        xpathStaticContext.declareVariable(null, it.name())
                    }

                    // Register PMD functions
                    Initializer.initialize(xpathStaticContext as IndependentContext)

                    xpathStaticContext.declareNamespace("pmd-java", "java:" + JavaFunctions::class.java.name)

                    val saxonTime: Long = try {
                        measureNanoTime { xpathEvaluator.createExpression(xpath) }
                    } catch (e: XPathException) {
                        // Saxon failed, probably incompatibility with 1.0
                        -1
                    }

                    Triple(root!!, time, saxonTime)
                } catchAnyParserError {
                    throw AssertionError("Parser failed, xpath is:\n\n$xpath\n\n", it)
                }

                var numNodes = 0
                // counts nodes
                object : XPathSideEffectingVisitor<Void> {
                    override fun visit(node: XPathNode, data:Void) {
                        numNodes += 1
                        return super.visit(node, data)
                    }
                }.let {
                    root.jjtAccept(it, null)
                }

                addTimingResult(TimingResult(time, saxonTime, numNodes, xpath.length, rule))
            }

        }
    }

    override fun listeners(): List<TestListener> = listOf(TimerListener)

    // Timing data is not very reliable
    // Though PMD's parser is consistently faster than Saxon's,
    // excluding a few outliers that change on each execution.

    internal companion object {

        data class TimingResult(val timeNano: Long, val saxonTimeNano: Long, val numNodes: Int, val sourceLength: Int, val rule: Rule) {

            override fun toString(): String {

                val diff = timeNano - saxonTimeNano
                val percent = (diff * 100 / saxonTimeNano)

                val saxonTimeFormatted = saxonTimeNano.takeIf { it >= 0 } ?: "---"

                val formatted = when {
                    saxonTimeNano < 0 -> "$ANSI_YELLOW---$ANSI_RESET" // saxon failed
                    percent > 0       -> "$ANSI_RED+$percent$ANSI_RESET"
                    else              -> "$ANSI_GREEN$percent$ANSI_RESET"
                }

                return String.format(ColFormat, saxonTimeFormatted, timeNano, formatted, numNodes, sourceLength, rule.name)
            }

            companion object {

                val ANSI_RED = "\u001B[31m"
                val ANSI_GREEN = "\u001B[32m"
                val ANSI_RESET = "\u001B[0m"
                val ANSI_YELLOW = "\u001B[33m"

                private val ColFormat = "%9s | %9s | %17s | %10s | %10s | %s"

                val HeaderLine = buildString {

                    appendln(String.format("%32s |",
                            center("Parse time (ms)", 32),
                            "",
                            "",
                            "",
                            "",
                            ""
                    ))

                    appendln(String.format("%9s | %9s | %5s | %10s | %10s | %s",
                            "Saxon",
                            "PMD",
                            "Diff (%)",
                            "AST size",
                            "Source len.",
                            "Rule name"
                    ))

                    appendln("-".repeat(70))
                }
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

                println(TimingResult.HeaderLine)
                results.sortedBy { it.rule.name }.forEach { println(it) }
            }
        }
    }
}
