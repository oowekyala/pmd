package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.ast.test.matchNode

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class TreeManipulationTest : FunSpec({


    parserTest("Test replacement") {

        val root = parseXPathRoot("1=2")


        root should matchNode<ASTXPathRoot> {
            child<ASTComparisonExpr> {
                child<ASTNumericLiteral> { }
                child<ASTNumericLiteral> { }
            }
        }

        root.getFirstDescendantOfType(ASTNumericLiteral::class.java).replaceWith(ASTStringLiteral("'hey'"))

        root should matchNode<ASTXPathRoot> {
            child<ASTComparisonExpr> {
                child<ASTStringLiteral> { it.unescapedValue shouldBe "hey" }
                child<ASTNumericLiteral> { }
            }
        }


    }


})