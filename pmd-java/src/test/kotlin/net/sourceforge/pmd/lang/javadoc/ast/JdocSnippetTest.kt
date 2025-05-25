/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldHaveText


class JdocSnippetTest : JdocParserTestSpec({

    parserTest("Test @snippet without attributes") {


        """
        /**
         * A simple program.
         * {@snippet :
         * class HelloWorld {
         *     public static void main(String... args) {
         *         System.out.println("Hello World!");  // @link substring="System.out" target="System#out"
         *     }
         * }
         * }
         */
        """.trimIndent() should parseAsJdoc {
            data("A simple program.")
            link {
                it shouldHaveText "{@link #hey}"
                it::getRef shouldBe fieldRef("hey") {
                    it shouldHaveText "#hey"
                    emptyClassRef()
                }
            }
        }
    }
})
