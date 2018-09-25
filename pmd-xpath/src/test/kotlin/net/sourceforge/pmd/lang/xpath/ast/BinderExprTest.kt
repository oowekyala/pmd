package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class BinderExprTest : FunSpec({

    parserTest("Test simple let expression") {

        "let \$i := 1 + 2 return \$i * 3" should matchExpr<ASTLetExpr> {

            val iBinding = child<ASTVarBinding> {
                it.varName shouldBe "i"

                child<ASTName> {
                    it.image shouldBe "i"
                }

                child<ASTAdditiveExpr>(ignoreChildren = true) {


                }
            }

            child<ASTMultiplicativeExpr> {
                child<ASTVarRef> {
                    it.binding shouldBe iBinding
                    it.isFree shouldBe false

                    it.varNameNode shouldBe child {
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


    testGroup("A variable's scope should not include its initialiser") {
        "let \$a := \$a + 2 return \$a" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.isFree shouldBe true
                        it.binding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.isFree shouldBe false
                it.binding shouldBe aBinding
                child<ASTName> { }
            }
        }

        "for \$a in \$a + 2 return \$a" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.isFree shouldBe true
                        it.binding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.isFree shouldBe false
                it.binding shouldBe aBinding
                child<ASTName> { }
            }
        }

        "some \$a in \$a + 2 satisfies \$a" should matchExpr<ASTQuantifiedExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.isFree shouldBe true
                        it.binding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.isFree shouldBe false
                it.binding shouldBe aBinding
                child<ASTName> { }
            }
        }
    }


    testGroup("A variable's scope should include the initialisers of the next variables") {
        "let \$a := 2, \$b := \$a return \$b" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }
                it.initializerExpr shouldBe child<ASTNumericLiteral> {}
            }

            val bBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTVarRef> {
                    it.isFree shouldBe false
                    it.binding shouldBe aBinding
                    child<ASTName> { }
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.isFree shouldBe false
                it.binding shouldBe bBinding
                child<ASTName> { }
            }

            it.bindings.shouldContainExactly(aBinding, bBinding)
        }

        "for \$a in (1,2), \$b in f(\$a) return \$b" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }
                unspecifiedChild()
            }

            val bBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTFunctionCall> {
                    child<ASTName> { }
                    child<ASTArgumentList> {
                        child<ASTArgument> {
                            child<ASTVarRef> {
                                it.isFree shouldBe false
                                it.binding shouldBe aBinding
                                child<ASTName> { }
                            }
                        }
                    }
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.isFree shouldBe false
                it.binding shouldBe bBinding
                child<ASTName> { }
            }

            it.bindings.shouldContainExactly(aBinding, bBinding)
        }
    }
})