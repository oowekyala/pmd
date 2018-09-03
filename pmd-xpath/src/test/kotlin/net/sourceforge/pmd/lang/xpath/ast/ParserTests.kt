package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ParserTests : FunSpec({


    parserTest("Test nested comments") {
        parseXPathRoot("(: (: we're nested :) :) //hello  ")
    }


    testGroup("Whitespace rules") {


        listOf("10 div3", "10div3", "foo- foo").forEach {

            // 10div 3 is actually valid in our lexical grammar, lexed as INTEGER_LITERAL NCNAME INTEGER_LITERAL
            // This is forbidden by the XPath spec: https://www.w3.org/TR/xpath-30/#id-terminal-delimitation
            // But honestly, whatever
            it should throwParseFailure()
        }


        "foo-foo" should matchExpr<ASTPathExpr> {
            child<ASTAxisStep> {
                child<ASTExactNameTest> {
                    it.nameNode shouldBe child { it.localName shouldBe "foo-foo" }
                }
            }
        }

        "foo -foo" should matchExpr<ASTAdditiveExpr> {
            unspecifiedChild()
            child<ASTAdditiveOperator> { it.image shouldBe "-" }
            unspecifiedChild()
        }

        "foo(: This is a comment :)- foo" should matchExpr<ASTAdditiveExpr> {
            unspecifiedChild()
            child<ASTAdditiveOperator> { it.image shouldBe "-" }
            unspecifiedChild()
        }

    }


})