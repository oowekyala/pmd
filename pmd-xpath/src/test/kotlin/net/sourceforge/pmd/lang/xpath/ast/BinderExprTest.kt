package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.util.*

/**
 * Tests about BinderExpr and scope/VarRef resolution
 *
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
                    it.binding shouldBe Optional.of(iBinding)

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


    testGroup("A variable's scope should not include its own initialiser") {
        "let \$a := \$a + 2 return \$a" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.binding shouldBe Optional.empty()
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.binding shouldBe Optional.of(aBinding)
                child<ASTName> { }
            }
        }

        "for \$a in \$a + 2 return \$a" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.binding shouldBe Optional.empty()
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.binding shouldBe Optional.of(aBinding)
                child<ASTName> { }
            }
        }

        "some \$a in \$a + 2 satisfies \$a" should matchExpr<ASTQuantifiedExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it.initializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it.binding shouldBe Optional.empty()
                        child<ASTName> { }
                    }
                    unspecifiedChildren(2)
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.binding shouldBe Optional.of(aBinding)
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
                    it.binding shouldBe Optional.of(aBinding)
                    child<ASTName> { }
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.binding shouldBe Optional.of(bBinding)
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
                                it.binding shouldBe Optional.of(aBinding)
                                child<ASTName> { }
                            }
                        }
                    }
                }
            }

            it.bodyExpr shouldBe child<ASTVarRef> {
                it.binding shouldBe Optional.of(bBinding)
                child<ASTName> { }
            }

            it.bindings.shouldContainExactly(aBinding, bBinding)
        }
    }

    testGroup("A variable binding should shadow other lexically enclosing bindings") {

        "let \$a := 2 return let \$a := 1 return \$a" should matchExpr<ASTLetExpr> {
            val fstABinding = child<ASTVarBinding> {
                it.varName shouldBe "a"

                child<ASTName> { }
                it.initializerExpr shouldBe child<ASTNumericLiteral> {}
            }

            it.bodyExpr shouldBe child<ASTLetExpr> {
                val sndABinding = child<ASTVarBinding> {
                    it.varName shouldBe "a"

                    child<ASTName> { }
                    it.initializerExpr shouldBe child<ASTNumericLiteral> { }
                }

                it.bodyExpr shouldBe child<ASTVarRef> {
                    it.binding shouldBe Optional.of(sndABinding) // ref to the second var
                    child<ASTName> { }
                }

                it.bindings.shouldContainExactly(sndABinding)
            }

            it.bindings.shouldContainExactly(fstABinding)
        }
    }

})