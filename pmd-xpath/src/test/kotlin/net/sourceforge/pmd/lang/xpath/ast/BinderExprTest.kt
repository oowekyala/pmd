package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 * Tests about BinderExpr and scope/VarRef resolution
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
class BinderExprTest : XPathParserTestSpec({

    parserTest("Test simple let expression") {

        "let \$i := 1 + 2 return \$i * 3" should matchExpr<ASTLetExpr> {

            val iBinding = child<ASTVarBinding> {
                it::getVarName shouldBe "i"

                child<ASTName> {
                    it::getImage shouldBe "i"
                }

                child<ASTAdditiveExpr>(ignoreChildren = true) {


                }
            }

            child<ASTMultiplicativeExpr> {

                it::getOperator shouldBe "*"

                child<ASTVarRef> {
                    it::getBinding shouldBe iBinding

                    it::getVarNameNode shouldBe child {
                        it::getLocalName shouldBe "i"
                    }
                }


                child<ASTNumericLiteral> {
                    it::getImage shouldBe "3"
                }
            }
        }
    }


    parserTest("A variable's scope should not include its own initialiser") {
        "let \$a := \$a + 2 return \$a" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChild()
                }
            }

            it::getBodyExpr shouldBe child<ASTVarRef> {
                it::getBinding shouldBe aBinding
                child<ASTName> { }
            }
        }

        "for \$a in \$a + 2 return \$a" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChild()
                }
            }

            it::getBodyExpr shouldBe child<ASTVarRef> {
                it::getBinding shouldBe aBinding
                child<ASTName> { }
            }
        }

        "some \$a in \$a + 2 satisfies \$a" should matchExpr<ASTQuantifiedExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe child<ASTAdditiveExpr> {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        child<ASTName> { }
                    }
                    unspecifiedChild()
                }
            }

            it::getBodyExpr shouldBe child<ASTVarRef> {
                it::getBinding shouldBe aBinding
                child<ASTName> { }
            }
        }
    }


    parserTest("A variable's scope should include the initialisers of the next variables") {
        "let \$a := 2, \$b := \$a return \$b" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }
                it::getInitializerExpr shouldBe child<ASTNumericLiteral> {}
            }

            val bBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe child<ASTVarRef> {
                    it::getBinding shouldBe aBinding
                    child<ASTName> { }
                }
            }

            it::getBodyExpr shouldBe child<ASTVarRef> {
                it::getBinding shouldBe bBinding
                child<ASTName> { }
            }

            it.bindings.toList().shouldContainExactly(aBinding, bBinding)
        }

        "for \$a in (1,2), \$b in f(\$a) return \$b" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }
                unspecifiedChild()
            }

            val bBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe child<ASTFunctionCall> {
                    child<ASTName> { }
                    child<ASTArgumentList> {
                        child<ASTArgument> {
                            child<ASTVarRef> {
                                it::getBinding shouldBe aBinding
                                child<ASTName> { }
                            }
                        }
                    }
                }
            }

            it::getBodyExpr shouldBe child<ASTVarRef> {
                it::getBinding shouldBe bBinding
                child<ASTName> { }
            }

            it.bindings.toList().shouldContainExactly(aBinding, bBinding)
        }
    }

    parserTest("A variable binding should shadow other lexically enclosing bindings") {

        "let \$a := 2 return let \$a := 1 return \$a" should matchExpr<ASTLetExpr> {
            val fstABinding = child<ASTVarBinding> {
                it::getVarName shouldBe "a"

                child<ASTName> { }
                it::getInitializerExpr shouldBe child<ASTNumericLiteral> {}
            }

            it::getBodyExpr shouldBe child<ASTLetExpr> {
                val sndABinding = child<ASTVarBinding> {
                    it::getVarName shouldBe "a"

                    child<ASTName> { }
                    it::getInitializerExpr shouldBe child<ASTNumericLiteral> { }
                }

                it::getBodyExpr shouldBe child<ASTVarRef> {
                    it::getBinding shouldBe sndABinding // ref to the second var
                    child<ASTName> { }
                }

                it.bindings.toList().shouldContainExactly(sndABinding)
            }

            it.bindings.toList().shouldContainExactly(fstABinding)
        }
    }

})