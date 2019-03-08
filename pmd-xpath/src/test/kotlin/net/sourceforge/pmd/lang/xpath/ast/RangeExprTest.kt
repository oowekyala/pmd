package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
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

                    it::getExpression shouldBe child<ASTRangeExpr> {
                        it::getLowerBound shouldBe child<ASTNumericLiteral> {}
                        it::getUpperBound shouldBe child<ASTNumericLiteral> {}
                    }
                }
            }
        }


        "(10, 1 to 4)" should matchExpr<ASTParenthesizedExpr> {

            it::getWrappedNode shouldBe child<ASTSequenceExpr> {

                child<ASTNumericLiteral> {
                    it::getIntValue shouldBe 10
                }

                child<ASTRangeExpr> {
                    it::getLowerBound shouldBe child<ASTNumericLiteral> {
                        it::getIntValue shouldBe 1
                    }
                    it::getUpperBound shouldBe child<ASTNumericLiteral> {
                        it::getIntValue shouldBe 4
                    }
                }
            }
        }
    }
})