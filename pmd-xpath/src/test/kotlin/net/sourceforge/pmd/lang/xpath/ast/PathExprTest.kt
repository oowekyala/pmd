package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.ROOT

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class PathExprTest : FunSpec({


    parserTest("Test path anchor") {

        "/A/B" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            val fstStep = child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.CHILD

                    child<ASTNodeTest> {
                        child<ASTName> {
                            it.image shouldBe "A"
                        }
                    }

                    child<ASTPredicateList> {

                    }
                }

            }


            val sndStep = child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.CHILD

                    child<ASTNodeTest> {

                        child<ASTName> {
                            it.image shouldBe "B"
                        }
                    }

                    child<ASTPredicateList> {

                    }
                }
            }

            it.iterator().asSequence().toList().shouldContainExactly(listOf(fstStep, sndStep))
        }

    }

    parserTest("Test attribute shorthand") {

        "/@A" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.ATTRIBUTE
                    it.isAbbrevAttributeAxis shouldBe true

                    child<ASTNodeTest> {
                        child<ASTName> {
                            it.image shouldBe "A"
                        }
                    }

                    child<ASTPredicateList> {

                    }
                }

            }
        }

    }


    parserTest("Test parent shorthand") {

        "/.." should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.PARENT
                    it.isAbbrevParentNodeTest shouldBe true

                    child<ASTNodeTest> {
                        child<ASTAnyKindTest> {

                        }
                    }

                    child<ASTPredicateList> {

                    }
                }

            }
        }

    }


    parserTest("Test double slash shorthand") {

        "/A//B" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT


            val fstStep = child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.CHILD

                    child<ASTNodeTest> {
                        child<ASTName> {
                            it.image shouldBe "A"
                        }
                    }

                    child<ASTPredicateList> {

                    }
                }

            }

            val sndStep = child<ASTStepExpr> {
                it.isAbbrevDescendantOrSelf shouldBe true

                // synthesized descendant-or-self::node()
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.DESCENDANT_OR_SELF

                    child<ASTNodeTest> {
                        child<ASTAnyKindTest> {

                        }
                    }

                    child<ASTPredicateList> {

                    }
                }

            }


            val thrdStep = child<ASTStepExpr> {
                child<ASTAxisStep> {

                    it.axis shouldBe Axis.CHILD

                    child<ASTNodeTest> {

                        child<ASTName> {
                            it.image shouldBe "B"
                        }
                    }

                    child<ASTPredicateList> {

                    }
                }
            }

            it.iterator().asSequence().toList().shouldContainExactly(listOf(fstStep, sndStep, thrdStep))
        }

    }


})