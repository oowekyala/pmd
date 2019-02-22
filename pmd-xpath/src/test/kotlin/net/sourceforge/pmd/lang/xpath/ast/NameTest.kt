package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.RELATIVE
import java.util.*

class NameTest : XPathParserTestSpec({


    parserTest("Test braced URI name") {

        val uri = "http://www.w3.org/2005/xpath-functions/math"
        val name = "Q{$uri}hendeck"

        name should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it::isUriLiteral shouldBe true
                        it::getExplicitNamespacePrefix shouldBe Optional.of(uri)
                        it::getLocalName shouldBe "hendeck"
                        it::getImage shouldBe name
                    }
                }
            }
        }
    }

    parserTest("Test namespaced name") {

        "pmd-java:typeIs(\"LolWhat\", ?)" should matchExpr<ASTFunctionCall> {

            it::getFunctionNameNode shouldBe child {
                it::isUriLiteral shouldBe false
                it::getExplicitNamespacePrefix shouldBe Optional.of("pmd-java")
                it::getLocalName shouldBe "typeIs"
                it::getImage shouldBe "pmd-java:typeIs"
            }

            it::getArguments shouldBe child {

                child<ASTArgument> {

                    it::isPlaceholder shouldBe false

                    child<ASTStringLiteral> {
                        it::getImage shouldBe "\"LolWhat\""
                        it::getUnescapedValue shouldBe "LolWhat"
                    }
                }

                child<ASTArgument> {
                    it::isPlaceholder shouldBe true
                }
            }
        }
    }

    parserTest("Test name without namespace") {

        "pmd-java" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {

                child<ASTExactNameTest> {
                    child<ASTName> {
                        it::isUriLiteral shouldBe false
                        it::getExplicitNamespacePrefix shouldBe Optional.empty()
                        it::getLocalName shouldBe "pmd-java"
                        it::getImage shouldBe "pmd-java"
                    }
                }
            }
        }
    }


})