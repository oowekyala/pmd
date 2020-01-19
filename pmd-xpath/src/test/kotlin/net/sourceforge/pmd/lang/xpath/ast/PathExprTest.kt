package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.ROOT

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class PathExprTest : XPathParserTestSpec({


    parserTest("Test path anchor") {

        "/A/B" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            val fstStep = child<ASTAxisStep> {

                it.axis shouldBe Axis.CHILD

                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it.nameImage shouldBe "A"

                    it.nameNode shouldBe child { it.image shouldBe "A" }
                }

            }


            val sndStep = child<ASTAxisStep> {

                it.axis shouldBe Axis.CHILD


                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it.nameImage shouldBe "B"

                    child<ASTName> { it.image shouldBe "B" }

                }
            }

            it.toList().shouldContainExactly(listOf(fstStep, sndStep))
        }
    }

    parserTest("Test attribute shorthand") {

        "/@A" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTAxisStep> {

                it.axis shouldBe Axis.ATTRIBUTE
                it.isAbbrevAttributeAxis shouldBe true

                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it.nameImage shouldBe "A"

                    child<ASTName> { it.image shouldBe "A" }
                }

            }
        }
    }


    parserTest("Test parent shorthand") {

        "/.." should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTAxisStep> {

                it.axis shouldBe Axis.PARENT
                it.isAbbrevParentNodeTest shouldBe true

                it.nodeTest shouldBe child<ASTAnyKindTest> {

                }

            }
        }
    }


    parserTest("Test double slash shorthand") {

        "/A//B" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT


            val fstStep = child<ASTAxisStep> {

                it.axis shouldBe Axis.CHILD

                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it.nameImage shouldBe "A"

                    child<ASTName> { it.image shouldBe "A" }
                }
            }


            // synthesized descendant-or-self::node()
            val sndStep = child<ASTAxisStep> {
                it.isAbbrevDescendantOrSelf shouldBe true

                it.axis shouldBe Axis.DESCENDANT_OR_SELF

                it.nodeTest shouldBe child<ASTAnyKindTest> {}
            }


            val thrdStep = child<ASTAxisStep> {

                it.axis shouldBe Axis.CHILD

                child<ASTExactNameTest> {
                    it.nameImage shouldBe "B"

                    child<ASTName> { it.image shouldBe "B" }
                }
            }


            it.toList().shouldContainExactly(listOf(fstStep, sndStep, thrdStep))
        }
    }

    parserTest("Test context item expr") {

        "./Foo" should matchExpr<ASTPathExpr> {

            child<ASTContextItemExpr> { }

            child<ASTAxisStep> {
                child<ASTExactNameTest> {
                    it.nameNode shouldBe child { it.localName shouldBe "Foo" }
                }
            }

        }
    }


    parserTest("Test one predicate") {

        "following-sibling::chapter[fn:position() = 1]" should matchExpr<ASTPathExpr> {

            child<ASTAxisStep> {
                it.axis shouldBe Axis.FOLLOWING_SIBLING

                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it::getNameNode shouldBe simpleName("chapter")
                }

                val pred = child<ASTPredicate> {
                    infixExpr(XpBinaryOp.EQ) {
                        child<ASTFunctionCall> {
                            it.functionNameNode shouldBe child {
                                it.localName shouldBe "position"
                                it.explicitNamespacePrefix shouldBe "fn"
                            }

                            child<ASTArgumentList> { }
                        }

                        int(1)
                    }
                }

                it.predicates.shouldContainExactly(pred)
            }
        }
    }

    parserTest("Test predicate with abbrev descendant or self") {
        "../A//N[@Image = 'Override']" should matchExpr<ASTPathExpr> {

            child<ASTAxisStep> {
                it.axis shouldBe Axis.PARENT
                unspecifiedChild()
            }
            child<ASTAxisStep> {
                it.axis shouldBe Axis.CHILD

                it.nodeTest shouldBe child<ASTExactNameTest> {
                    it::getNameNode shouldBe simpleName("A")
                }
            }

            // synthesized descendant-or-self::node()
            child<ASTAxisStep> {
                it.isAbbrevDescendantOrSelf shouldBe true

                it.axis shouldBe Axis.DESCENDANT_OR_SELF

                it.nodeTest shouldBe child<ASTAnyKindTest> {}
            }


            child<ASTAxisStep> {

                it.axis shouldBe Axis.CHILD

                child<ASTExactNameTest> {
                    it::getNameNode shouldBe simpleName("N")
                }

                child<ASTPredicate>(ignoreChildren = true) {}
            }
        }
    }
})
