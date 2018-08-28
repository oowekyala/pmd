package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ParserTests : FunSpec({


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
                }
            }
        }




    }

})