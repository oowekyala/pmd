/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast


class JdocBlockTagParserTests : JdocParserTestSpec({


    parserTest("Test @return block tag") {
        """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         * @return A return
         *  value
         */
        """.trimIndent() should parseAsJdoc {
            data("Param ")
            code("<T>")
            data(" is no ")
            code("<R>")
            blockTag("@return") {
                data("A return value")
            }

        }

    }


    parserTest("Test block tag precedence over HTML") {

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
        """.trimIndent() should parseAsJdoc {
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


})
