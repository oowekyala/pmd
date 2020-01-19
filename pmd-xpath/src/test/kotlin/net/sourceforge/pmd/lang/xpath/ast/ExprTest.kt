package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.DESCENDANT_OR_ROOT
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ExprTest : XPathParserTestSpec({





    parserTest("Test for expression") {

        "for \$i in //i return \$i" should matchExpr<ASTForExpr> {
            val iBinding = child<ASTVarBinding> {
                it::getVarName shouldBe "i"

                child<ASTName> {
                    it::getImage shouldBe "i"
                }

                child<ASTPathExpr> {

                    it::getPathAnchor shouldBe DESCENDANT_OR_ROOT

                    child<ASTAxisStep> {
                        child<ASTExactNameTest>(ignoreChildren = true) {
                            it::getNameImage shouldBe "i"
                        }
                    }
                }
            }


            child<ASTVarRef> {
                it::getBinding shouldBe iBinding

                it::getVarNameNode shouldBe child {
                    it::getLocalName shouldBe "i"
                }
            }
        }
    }


})
