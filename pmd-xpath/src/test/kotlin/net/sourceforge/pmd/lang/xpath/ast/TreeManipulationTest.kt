package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import net.sourceforge.pmd.lang.ast.test.matchNode

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class TreeManipulationTest : XPathParserTestSpec({


    parserTest("Test replacement") {

        val root = parseXPathRoot("1=2")


        root should matchNode<ASTXPathRoot> {
            infixExpr(XpBinaryOp.EQ) {
                int(1)
                int(2)
            }
        }

        root.getFirstDescendantOfType(ASTNumericLiteral::class.java).replaceWith(ASTStringLiteral("'hey'"))

        root should matchNode<ASTXPathRoot> {
            infixExpr(XpBinaryOp.EQ) {
                stringLit("hey")
                int(2)
            }
        }


    }


})
