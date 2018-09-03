package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.DESCENDANT_OR_ROOT

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ExprTest : FunSpec({


    parserTest("Test let expression") {

        "let ${'$'}i := 1 + 2 return ${'$'}i * 3" should matchRoot {
            child<ASTExpr> {
                child<ASTLetExpr> {

                    child<ASTVarBindingList> {
                        child<ASTVarBinding> {
                            it.varName shouldBe "i"

                            child<ASTName> {
                                it.image shouldBe "i"
                            }

                            child<ASTExpr>(ignoreChildren = true) {

                            }
                        }
                    }

                    child<ASTExpr> {
                        child<ASTMultiplicativeExpr> {
                            child<ASTVarRef> {
                                it.variableName shouldBe child {
                                    it.localName shouldBe "i"
                                }
                            }

                            child<ASTMultiplicativeOperator> { }

                            child<ASTNumericLiteral> {
                                it.image shouldBe "3"
                            }
                        }
                    }
                }
            }
        }
    }


    parserTest("Test for expression") {

        "for ${'$'}i in //i return ${'$'}i" should matchRoot {
            child<ASTExpr> {
                child<ASTForExpr> {

                    child<ASTVarBindingList> {
                        child<ASTVarBinding> {
                            it.varName shouldBe "i"

                            child<ASTName> {
                                it.image shouldBe "i"
                            }

                            child<ASTExpr> {
                                child<ASTPathExpr> {

                                    it.pathAnchor shouldBe DESCENDANT_OR_ROOT

                                    child<ASTAxisStep> {
                                        child<ASTExactNameTest>(ignoreChildren = true) {
                                            it.nameImage shouldBe "i"
                                        }
                                    }
                                }
                            }
                        }
                    }

                    child<ASTExpr> {
                        child<ASTVarRef> {
                            it.variableName shouldBe child {
                                it.localName shouldBe "i"
                            }
                        }
                    }
                }
            }
        }
    }


})