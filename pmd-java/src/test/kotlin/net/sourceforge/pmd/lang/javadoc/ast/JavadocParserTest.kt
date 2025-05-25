/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.EmptyAssertions
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.*
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink


class JavadocParserTest : JavadocParserSpec({


    parserTest("Test javadoc parser 1") {

        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link #hey}\n */"

            data("See ")
            link {
                it::getFieldName shouldBe "hey"
            }
        }


    }

    parserTest("Test some HTML") {

        """
/**
 *  <i> foo</i> <p> aha
 */
        """.trimIndent() should parseAs {

            html("i") {
                it::getText shouldBe "<i> foo</i>"
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it::getText shouldBe "<p> aha"
                data(" aha")
            }
        }
    }

    parserTest("Test nested HTML") {

        """
/**
 *  <i> foo</i> <p> aha
 */
        """.trimIndent() should parseAs {

            html("i") {
                it::getText shouldBe "<i> foo</i>"
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it::getText shouldBe "<p> aha"
                data(" aha")
            }
        }
    }
    parserTest("f:Autoclosing HTML") {

        """
/**
 * Header.
 *
 * <p>OHA
 * <ul>
 *     <li>LI one
 *     <li>LI two
 *     <p>LIP {@link net.sourceforge.pmd.lang.java.ast.JavaNode}
 *     <li>LI three
 *     </li>
 * </ul>
 *
 */
""".trimIndent() should parseAs {
            data("Header.")

            html("p") {
                data("OHA")
                html("ul") {
                    html("li") {
                        data("LI one")
                    }
                    html("li") {
                        data("LI two")
                        html("p") {
                            data("LIP ")
                            typeLink(name = "net.sourceforge.pmd.lang.java.ast.JavaNode")
                        }
                    }
                    html("li") {
                        data("LI three")
                        htmlEnd("li")
                    }
                    htmlEnd("ul")
                }
            }

        }
    }

})


fun TreeNodeWrapper<Node, out JavadocNode>.html(name: String, spec: NodeSpec<JdocHtml>) =
        child<JdocHtml> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.htmlEnd(name: String, spec: NodeSpec<JdocHtmlEnd> = EmptyAssertions) =
        child<JdocHtmlEnd> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.malformed(spec: NodeSpec<JdocMalformed>) =
        child<JdocMalformed> {
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.data(data: String, spec: NodeSpec<JdocCommentData> = EmptyAssertions) =
        child<JdocCommentData> {
            it::getData shouldBe data
            spec()
        }


fun TreeNodeWrapper<Node, out JavadocNode>.link(plain: Boolean = false, spec: NodeSpec<JdocLink> = EmptyAssertions) =
        child<JdocLink> {
            it::getTagName shouldBe if (plain) "@linkplain" else "@link"
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.typeLink(name: String, plain: Boolean = false, spec: NodeSpec<JdocLink> = EmptyAssertions) =
        link(plain) {
            it::getTypeName shouldBe name
            it::getFieldName shouldBe null
            it::getArgs shouldBe null
            spec()
        }

