package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class AdditiveExprTest : XPathParserTestSpec({


    parserTest("AdditiveExpr should be flat") {
        "1 + 2 + 3" should matchExpr<ASTAdditiveExpr> {
            it.operator shouldBe "+"

            child<ASTNumericLiteral> {
                it.image shouldBe "1"
            }
            child<ASTNumericLiteral> {
                it.image shouldBe "2"
            }
            child<ASTNumericLiteral> {
                it.image shouldBe "3"
            }
        }
    }



    parserTest("Different operators should push different additive exprs") {
        "1 + 2 - 3" should matchExpr<ASTAdditiveExpr> {
            it.operator shouldBe "+"

            child<ASTNumericLiteral> {
                it.image shouldBe "1"
            }
            child<ASTAdditiveExpr> {
                it.operator shouldBe "-"

                child<ASTNumericLiteral> {
                    it.image shouldBe "2"
                }
                child<ASTNumericLiteral> {
                    it.image shouldBe "3"
                }
            }
        }
    }


    parserTest("Additive expr should have greater precedence than multiplicative") {
        "1 + 2 - 3 * 4" should matchExpr<ASTAdditiveExpr> {
            it.operator shouldBe "+"

            child<ASTNumericLiteral> {
                it.image shouldBe "1"
            }
            child<ASTAdditiveExpr> {
                it.operator shouldBe "-"

                child<ASTNumericLiteral> {
                    it.image shouldBe "2"
                }

                child<ASTMultiplicativeExpr> {
                    it

                    child<ASTNumericLiteral> {
                        it.image shouldBe "3"
                    }

                    child<ASTNumericLiteral> {
                        it.image shouldBe "4"
                    }
                }
            }
        }
    }

})