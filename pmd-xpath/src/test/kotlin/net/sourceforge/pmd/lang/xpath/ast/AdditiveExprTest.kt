package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe

/**
 */
class AdditiveExprTest : XPathParserTestSpec({
//
//
//    parserTest("AdditiveExpr should be flat") {
//        "1 + 2 + 3" should matchExpr<ASTAdditiveExpr> {
//            it.operator shouldBe "+"
//
//            child<ASTNumericLiteral> {
//                it.image shouldBe "1"
//            }
//            child<ASTNumericLiteral> {
//                it.image shouldBe "2"
//            }
//            child<ASTNumericLiteral> {
//                it.image shouldBe "3"
//            }
//        }
//    }



    parserTest("Different operators should push different additive exprs") {
        "1 + 2 - 3" should matchExpr<ASTInfixExpr> {
            it.operator shouldBe XpBinaryOp.SUB

            infixExpr(XpBinaryOp.ADD) {
                int(1)
                int(2)
            }
            int(3)
        }
    }


    parserTest("Additive expr should have greater precedence than multiplicative") {
        // (1+2)-(3*4))
        "1 + 2 - 3 * 4" should matchExpr<ASTInfixExpr> {
            it.operator shouldBe XpBinaryOp.SUB

            infixExpr(XpBinaryOp.ADD) {
                int(1)
                int(2)
            }

            infixExpr(XpBinaryOp.MUL) {
                int(3)
                int(4)
            }
        }


        // ((1+2)-(3*4))+5
        "1 + 2 - 3 * 4 + 5" should matchExpr<ASTInfixExpr> {
            it.operator shouldBe XpBinaryOp.ADD

            infixExpr(XpBinaryOp.SUB) {
                infixExpr(XpBinaryOp.ADD){
                    int(1)
                    int(2)
                }

                infixExpr(XpBinaryOp.MUL) {
                    int(3)
                    int(4)
                }
            }

            int(5)
        }
    }

})
