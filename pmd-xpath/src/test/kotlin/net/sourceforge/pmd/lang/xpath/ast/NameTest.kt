package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.RELATIVE

class NameTest : FunSpec({


    parserTest("Test braced URI name") {

        val uri = "http://www.w3.org/2005/xpath-functions/math"
        val name = "Q{$uri}pi"

        name should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it.isUriLiteral shouldBe true
                        it.namespacePrefix shouldBe uri
                        it.localName shouldBe "pi"
                        it.hasNamespacePrefix() shouldBe true
                        it.image shouldBe name
                    }
                }
            }
        }
    }

    parserTest("Test namespaced name") {

        "pmd-java:typeIs(\"LolWhat\", ?)" should matchRoot {

            child<ASTFunctionCall> {

                it.functionName shouldBe child {
                    it.isUriLiteral shouldBe false
                    it.namespacePrefix shouldBe "pmd-java"
                    it.localName shouldBe "typeIs"
                    it.hasNamespacePrefix() shouldBe true
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
    }

    parserTest("Test name without namespace") {

        "pmd-java" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it.isUriLiteral shouldBe false
                        it.namespacePrefix shouldNotBe null
                        it.namespacePrefix.shouldBeEmpty()
                        it.localName shouldBe "pmd-java"
                        it.hasNamespacePrefix() shouldBe false
                        it.image shouldBe "pmd-java"
                    }
                }
            }
        }
    }


})