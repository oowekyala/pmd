package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 */
class RangeExprTest : XPathParserTestSpec({

    parserTest("Test RangeExpr") {


        "fn:reverse(10 to 15)" should matchExpr<ASTFunctionCall> {

            it::getFunctionNameNode shouldBe child {
                it::getImage shouldBe "fn:reverse"
                it::getLocalName shouldBe "reverse"
                it::getExplicitNamespacePrefix shouldBe "fn"
                it::isUriLiteral shouldBe false
            }
            it::getArguments shouldBe child {

                child<ASTArgument> {
                    it::isPlaceholder shouldBe false

                    it::getExpression shouldBe infixExpr(XpBinaryOp.RANGE) {
                        int(10)
                        int(15)
                    }
                }
            }
        }


        "(10, 1 to 4)" should matchExpr<ASTSequenceExpr> {

            child<ASTNumericLiteral> {
                it::getIntValue shouldBe 10
            }

            infixExpr(XpBinaryOp.RANGE) {
                int(1)
                int(4)
            }
        }
    }
})
