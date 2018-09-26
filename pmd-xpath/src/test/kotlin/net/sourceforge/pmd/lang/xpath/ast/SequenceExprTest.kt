package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldBeEmpty
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
                it.size shouldBe 3
            }
        }
    }


    parserTest("Test empty sequence") {

        "()" should matchExpr<ASTEmptySequenceExpr> {
            it.isEmpty shouldBe true
            it.toList().shouldBeEmpty()
        }
    }



    parserTest("Test nested sequences") {

        "(1, (2, 3), ())" should matchExpr<ASTParenthesizedExpr> {
            child<ASTSequenceExpr> {
                it.isEmpty shouldBe false

                val fst = child<ASTNumericLiteral> { }
                val snd = child<ASTParenthesizedExpr> {
                    child<ASTSequenceExpr> {
                        child<ASTNumericLiteral> {  }
                        child<ASTNumericLiteral> {  }
                    }
                }
                val thrd = child<ASTEmptySequenceExpr> {
                    it.isEmpty shouldBe true
                    it.size shouldBe 0
                }

                it.size shouldBe 3
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