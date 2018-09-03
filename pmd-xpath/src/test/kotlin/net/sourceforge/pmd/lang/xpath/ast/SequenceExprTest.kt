package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class SequenceExprTest : FunSpec({


    parserTest("Test unparenthesized sequence") {

        "1,2/a,3 union 6" should matchExpr<ASTSequenceExpr> {
            val fst = child<ASTNumericLiteral> { it.intValue shouldBe 1 }
            val snd = child<ASTPathExpr>(ignoreChildren = true) { }
            val thrd = child<ASTUnionExpr>(ignoreChildren = true) { }

            it.toList().shouldContainExactly(fst, snd, thrd)
        }

    }



    parserTest("Test parenthesized sequence") {

        "(1,2/a,3 union 6)" should matchExpr<ASTParenthesizedExpr> {
            child<ASTSequenceExpr> {
                val fst = child<ASTNumericLiteral> { it.intValue shouldBe 1 }
                val snd = child<ASTPathExpr>(ignoreChildren = true) { }
                val thrd = child<ASTUnionExpr>(ignoreChildren = true) { }

                it.toList().shouldContainExactly(fst, snd, thrd)
            }
        }
    }


    parserTest("Test priority with if") {

        "if (true()) then 4 else 5, 6" should matchExpr<ASTSequenceExpr> {
            child<ASTIfExpr>(ignoreChildren = true) { }
            child<ASTNumericLiteral> { }
        }

    }


    parserTest("Test priority with if (failure)") {
        expect<ParseException>() whenParsing {
            "if (true()) then 4,5 else 5"
        }
    }


    parserTest("Test priority overriding with if") {
        "if (true()) then (4,5) else 5" should matchExpr<ASTIfExpr> {
            child<ASTFunctionCall>(ignoreChildren = true) { }
            child<ASTParenthesizedExpr> {
                child<ASTSequenceExpr>(ignoreChildren = true) { }
            }
            child<ASTNumericLiteral> { }
        }

    }


})