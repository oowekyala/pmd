package net.sourceforge.pmd.lang.xpath.opti

import io.kotlintest.should
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.matchNode
import net.sourceforge.pmd.lang.xpath.ast.*
import net.sourceforge.pmd.properties.IntegerProperty
import net.sourceforge.pmd.properties.PropertyDescriptor

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class PropertyInliningTest : XPathParserTestSpec({


    parserTest("Test IntProperty") {

        fun matcherWithHole(hole: NodeSpec<ASTComparisonExpr>): Assertions<Node?> = matchNode<ASTXPathRoot> {
            child<ASTPathExpr> {

                child<ASTAxisStep> {
                    child<ASTExactNameTest> {
                        it.nameImage shouldBe "abc"
                        child<ASTName> { }
                    }
                    child<ASTPredicate> {
                        child<ASTComparisonExpr> {
                            it.operatorImage shouldBe "="

                            child<ASTPathExpr>(ignoreChildren = true) {}

                            hole() // This node is replaced
                        }
                    }
                }
            }
        }

        val expr = "//abc[@Size=\$int]"

        parseXPathRoot(expr) should matcherWithHole {
            child<ASTVarRef> {
                it.varName shouldBe "int"
                child<ASTName> { }
            }
        }

        val property = IntegerProperty.named("int").defaultValue(2).range(1, 5).desc("test").build()
        val pMap = mapOf<PropertyDescriptor<*>, Int>(property to 2)


        val query = XPathOptimisationFacade().makeQuery(expr, pMap) as XPathQueryImpl
        query.optimise()

        query.root should matcherWithHole {
            child<ASTNumericLiteral> {
                it.intValue shouldBe 2
                it.isIntegerLiteral shouldBe true
            }
        }
    }
})