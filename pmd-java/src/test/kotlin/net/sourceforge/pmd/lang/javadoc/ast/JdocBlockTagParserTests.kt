/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import net.sourceforge.pmd.lang.java.ast.JavadocParsingCtx
import net.sourceforge.pmd.lang.java.ast.ProcessorTestSpec


class JdocBlockTagParserTests : ProcessorTestSpec({


    parserTest("Test @return block tag") {
        inContext(JavadocParsingCtx) {
            """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         * @return A return
         *  value
         */
        """.trimIndent() should parseAs {
                jdoc {
                    data("Param ")
                    code("<T>")
                    data(" is no ")
                    code("<R>")
                    blockTag("@return") {
                        data("A return value")
                    }
                }
            }
        }
    }


    parserTest("Test block tag precedence over HTML") {
        inContext(JavadocParsingCtx) {

            """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         * @return A return
         *  value <pre>
         *
         * @author name
         * </pre>
         */
        """.trimIndent() should parseAs {
                jdoc {
                    data("Param ")
                    code("<T>")
                    data(" is no ")
                    code("<R>")
                    blockTag("@return") {
                        data("A return value ")
                        html("pre") {

                        }
                    }
                    blockTag("@author") {
                        data("name")
                        htmlEnd("pre")
                    }
                }
            }
        }
    }


})
