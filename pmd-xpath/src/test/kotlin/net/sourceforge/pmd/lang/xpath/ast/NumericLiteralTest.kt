package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class NumericLiteralTest : FunSpec({


    parserTest("Test integer literal") {

        "123" should matchExpr<ASTNumericLiteral> {
            it::isIntegerLiteral shouldBe true
            it::isDecimalLiteral shouldBe false
            it::isDoubleLiteral shouldBe false

            it::getIntValue shouldBe 123
            it::getDoubleValue shouldBe 123.0

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.0
            it::getExponent shouldBe 0
        }
    }

    parserTest("Test decimal literal no decimal part") {

        "123." should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe true
            it::isDoubleLiteral shouldBe false

            it::getIntValue shouldBe 123
            it::getDoubleValue shouldBe 123.0

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.0
            it::getExponent shouldBe 0
        }
    }

    parserTest("Test decimal literal no integer part") {

        ".123" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe true
            it::isDoubleLiteral shouldBe false

            it::getIntValue shouldBe 0
            it::getDoubleValue shouldBe 0.123

            it::getIntegerPart shouldBe 0
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe 0
        }
    }


    parserTest("Test decimal literal") {

        "123.123" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe true
            it::isDoubleLiteral shouldBe false

            it::getIntValue shouldBe 123
            it::getDoubleValue shouldBe 123.123

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe 0
        }
    }


    parserTest("Test double literal") {

        "123.123e3" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe false
            it::isDoubleLiteral shouldBe true

            it::getIntValue shouldBe 123123
            it::getDoubleValue shouldBe 123123.0

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe 3
        }
    }

    parserTest("Test signed exponent") {

        "123.123e+3" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe false
            it::isDoubleLiteral shouldBe true

            it::getIntValue shouldBe 123123
            it::getDoubleValue shouldBe 123123.0

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe 3
        }
    }

    parserTest("Test negative exponent") {

        "123.123e-3" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe false
            it::isDoubleLiteral shouldBe true

            it::getIntValue shouldBe 0
            it::getDoubleValue shouldBe (0.123123 plusOrMinus 1e-8)

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe -3
        }
    }

    parserTest("Test zero exponent") {

        "123.123e-0" should matchExpr<ASTNumericLiteral> {

            it::isIntegerLiteral shouldBe false
            it::isDecimalLiteral shouldBe false
            it::isDoubleLiteral shouldBe true

            it::getIntValue shouldBe 123
            it::getDoubleValue shouldBe 123.123

            it::getIntegerPart shouldBe 123
            it::getDecimalPart shouldBe 0.123
            it::getExponent shouldBe 0
        }
    }


})