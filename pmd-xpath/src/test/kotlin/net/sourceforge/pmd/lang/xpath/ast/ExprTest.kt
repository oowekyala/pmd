package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.DESCENDANT_OR_ROOT
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ExprTest : FunSpec({





    parserTest("Test for expression") {

        "for ${'$'}i in //i return ${'$'}i" should matchExpr<ASTForExpr> {
            val iBinding = child<ASTVarBinding> {
                it.varName shouldBe "i"

                child<ASTName> {
                    it.image shouldBe "i"
                }

                child<ASTPathExpr> {

                    it.pathAnchor shouldBe DESCENDANT_OR_ROOT

                    child<ASTAxisStep> {
                        child<ASTExactNameTest>(ignoreChildren = true) {
                            it.nameImage shouldBe "i"
                        }
                    }
                }
            }


            child<ASTVarRef> {
                it.binding shouldBe Optional.of(iBinding)

                it.varNameNode shouldBe child {
                    it.localName shouldBe "i"
                }
            }
        }
    }


})