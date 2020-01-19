package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 */
class SequenceExprTest : XPathParserTestSpec({


    parserTest("Test unparenthesized sequence") {

        "1,2/a,3 union 6" should matchExpr<ASTSequenceExpr> {
            val fst = int(1)
            val snd = child<ASTPathExpr>(ignoreChildren = true) { }
            val thrd = infixExpr(XpBinaryOp.UNION) { int(3); int(6) }

            it.toList().shouldContainExactly(fst, snd, thrd)
        }

    }



    parserTest("Test parenthesized sequence") {

        "(1,2/a,3 union 6)" should matchExpr<ASTSequenceExpr> {
            val fst = child<ASTNumericLiteral> { it.intValue shouldBe 1 }
            val snd = child<ASTPathExpr>(ignoreChildren = true) { }
            val thrd = infixExpr(XpBinaryOp.UNION) { int(3); int(6) }

            it.toList().shouldContainExactly(fst, snd, thrd)
            it.size shouldBe 3
        }
    }


    parserTest("Test empty sequence") {

        "()" should matchExpr<ASTEmptySequenceExpr> {
            it::isEmpty shouldBe true
            it.toList().shouldBeEmpty()
        }
    }



    parserTest("Test nested sequences") {

        "(1, (2, 3), ())" should matchExpr<ASTSequenceExpr> {
            it::isEmpty shouldBe false

            val fst = int(1)
            val snd = child<ASTSequenceExpr> {
                int(2)
                int(3)
            }

            val thrd = child<ASTEmptySequenceExpr> {
                it::isEmpty shouldBe true
                it.size shouldBe 0
            }

            it.size shouldBe 3
            it.toList().shouldContainExactly(fst, snd, thrd)
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
            child<ASTSequenceExpr>(ignoreChildren = true) { }
            int(5)
        }

    }


})
