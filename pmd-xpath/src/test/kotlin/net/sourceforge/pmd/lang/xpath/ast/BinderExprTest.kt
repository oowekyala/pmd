package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainExactly
import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 * Tests about BinderExpr and scope/VarRef resolution
 *
 */
class BinderExprTest : XPathParserTestSpec({

    parserTest("Test simple let expression") {

        "let \$i := 1 + 2 return \$i * 3" should matchExpr<ASTLetExpr> {

            val iBinding = child<ASTVarBinding> {
                it::getVarName shouldBe "i"

                it::getVarNameNode shouldBe simpleName("i")

                infixExpr(XpBinaryOp.ADD) {
                    int(1)
                    int(2)
                }
            }

            infixExpr(XpBinaryOp.MUL) {
                simpleVarRef("i") {
                    it::getBinding shouldBe iBinding
                }
                int(3)
            }
        }
    }


    parserTest("A variable's scope should not include its own initialiser") {
        "let \$a := \$a + 2 return \$a" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                child<ASTName> { }

                it::getInitializerExpr shouldBe infixExpr(XpBinaryOp.ADD) {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        it::getVarNameNode shouldBe simpleName("a")
                    }
                    int(2)
                }
            }

            it::getBodyExpr shouldBe simpleVarRef("a") {
                it::getBinding shouldBe aBinding
            }
        }

        "for \$a in \$a + 2 return \$a" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                it::getVarNameNode shouldBe simpleName("a")

                it::getInitializerExpr shouldBe infixExpr(XpBinaryOp.ADD) {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        it::getVarNameNode shouldBe simpleName("a")
                    }
                    int(2)
                }
            }

            it::getBodyExpr shouldBe simpleVarRef("a") {
                it::getBinding shouldBe aBinding
            }
        }

        "some \$a in \$a + 2 satisfies \$a" should matchExpr<ASTQuantifiedExpr> {
            val aBinding = child<ASTVarBinding> {
                it::getVarNameNode shouldBe simpleName("a")

                it::getInitializerExpr shouldBe infixExpr(XpBinaryOp.ADD) {
                    child<ASTVarRef> {
                        it::getBinding shouldBe null
                        it::getVarNameNode shouldBe simpleName("a")
                    }
                    int(2)
                }
            }

            it::getBodyExpr shouldBe simpleVarRef("a") {
                it::getBinding shouldBe aBinding
            }
        }
    }


    parserTest("A variable's scope should include the initialisers of the next variables") {
        "let \$a := 2, \$b := \$a return \$b" should matchExpr<ASTLetExpr> {
            val aBinding = child<ASTVarBinding> {
                it::getVarNameNode shouldBe simpleName("a")
                it::getInitializerExpr shouldBe int(2)
            }

            val bBinding = child<ASTVarBinding> {
                it::getVarNameNode shouldBe simpleName("b")

                it::getInitializerExpr shouldBe simpleVarRef("a") {
                    it::getBinding shouldBe aBinding
                }
            }

            it::getBodyExpr shouldBe simpleVarRef("b") {
                it::getBinding shouldBe bBinding
            }

            it.bindings.toList().shouldContainExactly(aBinding, bBinding)
        }

        "for \$a in (1,2), \$b in f(\$a) return \$b" should matchExpr<ASTForExpr> {
            val aBinding = child<ASTVarBinding> {
                it::getVarNameNode shouldBe simpleName("a")
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
                it::getVarNameNode shouldBe simpleName("a")
                it::getInitializerExpr shouldBe int(2)
            }

            it::getBodyExpr shouldBe child<ASTLetExpr> {
                val sndABinding = child<ASTVarBinding> {
                    it::getVarNameNode shouldBe simpleName("a")
                    it::getInitializerExpr shouldBe int(1)
                }

                it::getBodyExpr shouldBe simpleVarRef("a") {
                    it::getBinding shouldBe sndABinding // ref to the second var
                }

                it.bindings.toList().shouldContainExactly(sndABinding)
            }

            it.bindings.toList().shouldContainExactly(fstABinding)
        }
    }

})
