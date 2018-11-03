package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class RangeExprTest : FunSpec({


    testGroup("Test RangeExpr") {


        "fn:reverse(10 to 15)" should matchExpr<ASTFunctionCall> {

            it.functionNameNode shouldBe child {
                it.image shouldBe "fn:reverse"
                it.localName shouldBe "reverse"
                it.explicitNamespacePrefix shouldBe Optional.of("fn")
                it.isUriLiteral shouldBe false
            }
            it.arguments shouldBe child {

                child<ASTArgument> {
                    it.isPlaceholder shouldBe false

                    it.expression shouldBe childOpt<ASTRangeExpr> {
                        it.lowerBound shouldBe child<ASTNumericLiteral> {}
                        it.upperBound shouldBe child<ASTNumericLiteral> {}
                    }
                }
            }
        }


        "(10, 1 to 4)" should matchExpr<ASTParenthesizedExpr> {

            it.wrappedNode shouldBe child<ASTSequenceExpr> {

                child<ASTNumericLiteral> {
                    it.intValue shouldBe 10
                }

                child<ASTRangeExpr> {
                    it.lowerBound shouldBe child<ASTNumericLiteral> {
                        it.intValue shouldBe 1
                    }
                    it.upperBound shouldBe child<ASTNumericLiteral> {
                        it.intValue shouldBe 4
                    }
                }
            }
        }
    }
})