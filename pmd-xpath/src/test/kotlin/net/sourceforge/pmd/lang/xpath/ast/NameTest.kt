package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.RELATIVE
import java.util.*

class NameTest : FunSpec({


    parserTest("Test braced URI name") {

        val uri = "http://www.w3.org/2005/xpath-functions/math"
        val name = "Q{$uri}hendeck"

        name should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it.isUriLiteral shouldBe true
                        it.explicitNamespacePrefix shouldBe Optional.of(uri)
                        it.localName shouldBe "hendeck"
                        it.image shouldBe name
                    }
                }
            }
        }
    }

    parserTest("Test namespaced name") {

        "pmd-java:typeIs(\"LolWhat\", ?)" should matchExpr<ASTFunctionCall> {

            it.functionNameNode shouldBe child {
                it.isUriLiteral shouldBe false
                it.explicitNamespacePrefix shouldBe Optional.of("pmd-java")
                it.localName shouldBe "typeIs"
                it.image shouldBe "pmd-java:typeIs"
            }

            it.arguments shouldBe child {

                child<ASTArgument> {

                    it.isPlaceholder shouldBe false

                    child<ASTStringLiteral> {
                        it.image shouldBe "\"LolWhat\""
                        it.unescapedValue shouldBe "LolWhat"
                    }
                }

                child<ASTArgument> {
                    it.isPlaceholder shouldBe true
                }
            }
        }
    }

    parserTest("Test name without namespace") {

        "pmd-java" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it.isUriLiteral shouldBe false
                        it.explicitNamespacePrefix shouldBe Optional.empty()
                        it.localName shouldBe "pmd-java"
                        it.image shouldBe "pmd-java"
                    }
                }
            }
        }
    }


})