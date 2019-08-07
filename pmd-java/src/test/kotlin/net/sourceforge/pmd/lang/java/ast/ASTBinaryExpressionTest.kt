/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.ast

import net.sourceforge.pmd.lang.java.ast.BinaryOp.*
import net.sourceforge.pmd.lang.java.ast.ParserTestCtx.Companion.ExpressionParsingCtx

/*
    Tests for the rest of binary expressions
 */
class ASTBinaryExpressionTest : ParserTestSpec({


    parserTest("Conditional and logical operators") {

        inContext(ExpressionParsingCtx) {
            "a && b && a || b" should parseAs {
                binaryExpr(CONDITIONAL_OR) {
                    binaryExpr(CONDITIONAL_AND) {
                        binaryExpr(CONDITIONAL_AND) {
                            variableAccess("a")
                            variableAccess("b")
                        }
                        variableAccess("a")
                    }

                    variableAccess("b")
                }
            }

            "a && b && a | b" should parseAs {
                binaryExpr(CONDITIONAL_AND) {
                    binaryExpr(CONDITIONAL_AND) {
                        variableAccess("a")
                        variableAccess("b")
                    }
                    binaryExpr(OR) {
                        variableAccess("a")
                        variableAccess("b")
                    }
                }
            }

            "a | b ^ a & b" should parseAs {
                binaryExpr(OR) {
                    variableAccess("a")
                    binaryExpr(XOR) {
                        variableAccess("b")
                        binaryExpr(AND) {
                            variableAccess("a")
                            variableAccess("b")
                        }
                    }
                }
            }
        }
    }

})
