package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.RELATIVE

/**
 */
class WildcardTest : XPathParserTestSpec({

    parserTest("Test full wildcard") {

        "*" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it::isFullWildcard shouldBe true

                    it::getExpectedLocalName shouldBe null
                    it::getExpectedNamespacePrefix shouldBe null
                    it::getExpectedNamespaceUri shouldBe null

                }
            }
        }
    }

    parserTest("Test local name wildcard") {

        "np:*" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it::getExpectedLocalName shouldBe null
                    it::getExpectedNamespacePrefix shouldBe "np"
                    it::getExpectedNamespaceUri shouldBe null
                }
            }
        }
    }

    parserTest("Test prefix wildcard") {

        "*:local" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it::getExpectedLocalName shouldBe "local"
                    it::getExpectedNamespacePrefix shouldBe null
                    it::getExpectedNamespaceUri shouldBe null
                }
            }
        }
    }

    parserTest("Test URI wildcard") {

        "Q{https://foo.com}*" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe RELATIVE

            child<ASTAxisStep> {
                child<ASTWildcardNameTest> {
                    it::getExpectedLocalName shouldBe null
                    it::getExpectedNamespacePrefix shouldBe null
                    it::getExpectedNamespaceUri shouldBe "https://foo.com"
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
