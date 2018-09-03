package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.RELATIVE

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class WildcardTest : FunSpec({

    parserTest("Test full wildcard") {

        "*" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it.isFullWildcard shouldBe true

                    it.expectedLocalName shouldBe null
                    it.expectedNamespacePrefix shouldBe null
                    it.expectedNamespaceUri shouldBe null

                }
            }
        }
    }

    parserTest("Test local name wildcard") {

        "np:*" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it.expectedLocalName shouldBe null
                    it.expectedNamespacePrefix shouldBe "np"
                    it.expectedNamespaceUri shouldBe null
                }
            }
        }
    }

    parserTest("Test prefix wildcard") {

        "*:local" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it.expectedLocalName shouldBe "local"
                    it.expectedNamespacePrefix shouldBe null
                    it.expectedNamespaceUri shouldBe null
                }
            }
        }
    }

    parserTest("Test URI wildcard") {

        "Q{https://foo.com}*" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it.expectedLocalName shouldBe null
                    it.expectedNamespacePrefix shouldBe null
                    it.expectedNamespaceUri shouldBe "https://foo.com"
                }
            }
        }
    }

    parserTest("Test whitespace handling") {

        listOf("pre: *", "pre : *", "pre :*",
                "* :loc", "* : loc", "*: loc",
                "Q{https://foo.com} *").forEach {
            expect<ParseException>() whenParsing { it }
        }

    }
})