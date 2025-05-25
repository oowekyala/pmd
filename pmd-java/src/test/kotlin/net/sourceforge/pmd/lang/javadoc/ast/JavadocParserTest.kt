/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink


class JavadocParserTest : JavadocParserSpec({


    parserTest("Test javadoc parser 1") {

        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAs {

            child<JdocCommentData> {
                it::getData shouldBe "See "
            }
            child<JdocLink> {
                it::getTagName shouldBe "@link"
                it::getFieldName shouldBe "hey"
            }
        }


    }

})
