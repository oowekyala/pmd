package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.should
import io.kotlintest.specs.FunSpec

/**
 * Tests the xpath expression dumping.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
class ExpressionMakerVisitorTest : FunSpec({


    parserTest("Test predicate with abbrev descendant or self") {
        "../A//N[@Image = 'Override']" should dumpAnEquivalentParsableString()
    }

    testGroup("Test inline function expr") {

        listOf("function() as xs:integer+ { 2, 3, 5, 7, 11, 13 }",
                "function(${'$'}a as xs:double, ${'$'}b as xs:double) as xs:double { ${'$'}a * ${'$'}b }",
                "function(${'$'}a) { ${'$'}a }",
                "collection()/(let ${'$'}a := . return function() { ${'$'}a })")
                .forEach {
                    it should dumpAnEquivalentParsableString()
                }
    }

    parserTest("Test instance of expr") {
        "(5, 6) instance of xs:integer+" should dumpAnEquivalentParsableString()
    }
})

class ExpressionMakerVisitorRuleRegressionTest : FunSpec({
    foreachXPathRule { rule, xpath ->
        parserTest("Test read/write cycle on ${rule.name} (${rule.ruleSetName})") {
            xpath should dumpAnEquivalentParsableString()
        }
    }
})


private fun ParserTestCtx.dumpAnEquivalentParsableString(): Matcher<String> = object : Matcher<String> {
    override fun test(value: String): Result {

        val fstDump = parseXPathRoot(value).toExpressionString()

        val sndDump = {
            parseXPathRoot(fstDump).toExpressionString()
        } catchAnyParserError {
            throw AssertionError("Parser failed, xpath was:\n\n$value\n\nnow is:\n\n$fstDump\n\n", it)
        }

        return Result(fstDump == sndDump, "The first dump wasn't equivalent to the second", "Aïe")
    }
}