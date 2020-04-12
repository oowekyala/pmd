/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import net.sourceforge.pmd.lang.java.ast.block


class JdocBlockTagParserTests : JavadocParserSpec({



    parserTest("Test @return block tag") {


        """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         * @return A return
         *  value
         */
        """.trimIndent() should parseAs {

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
        """.trimIndent() should parseAs {

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



})
