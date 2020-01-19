package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe


/**
 * @author Clément Fournier
 * @since 7.0.0
 */
class UnaryExprTest : XPathParserTestSpec({
    parserTest("Test UnaryExpr") {

        "+1" should matchExpr<ASTUnaryExpr> {
            it::getOperator shouldBe XpUnaryOp.PLUS
            it::getOperand shouldBe int(1)
        }

        "-++1" should matchExpr<ASTUnaryExpr> {
            it::getOperator shouldBe XpUnaryOp.MINUS
            child<ASTUnaryExpr> {
                it::getOperator shouldBe XpUnaryOp.PLUS
                child<ASTUnaryExpr> {
                    it::getOperator shouldBe XpUnaryOp.PLUS
                    it::getOperand shouldBe int(1)
                }
            }
        }
    }
})
