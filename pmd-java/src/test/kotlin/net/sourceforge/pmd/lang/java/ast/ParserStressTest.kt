/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import net.sourceforge.pmd.lang.ast.test.shouldMatchN

/**
 *
 */
class ParserStressTest : ParserTestSpec({

    // 0 + 1 + 2 + 3 + ...
    val exprString = (1 until 4000).fold(StringBuilder("0")) { a, i ->
        a.append("\n+ ").append(i)
    }.toString()


    parserTest("Test very deep InfixExpression doesn't throw StackOverflowError") {

        inContext(ExpressionParsingCtx) {

            doTest("Very big expr") {

                doParse(exprString).shouldMatchN {
                    infixExpr(BinaryOp.ADD) {
                        unspecifiedChild()
                        unspecifiedChild()
                    }
                }
            }
        }
    }


})
