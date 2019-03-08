package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 * @since 7.0.0
 */
class UnaryExprTest : XPathParserTestSpec({
    parserTest("Test UnaryExpr") {

        "+1" should matchExpr<ASTUnaryExpr> {
            it.operator shouldBe "+"
            it.operand shouldBe child<ASTNumericLiteral> { }
        }

        "-++1" should matchExpr<ASTUnaryExpr> {
            it.operator shouldBe "-++"
            it.operand shouldBe child<ASTNumericLiteral> { }
        }
    }
})