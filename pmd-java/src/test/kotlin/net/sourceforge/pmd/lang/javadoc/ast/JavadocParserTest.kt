/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.EmptyAssertions
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.*
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml.HtmlCloseSyntax
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.*
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLink
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.JdocLiteral
import kotlin.streams.toList


class JavadocParserTest : JavadocParserSpec({
    /*
        TODO tests:
         - case sensitivity
         - html comments
    */


    parserTest("Test some inline tags") {

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


        """
        /**
         * Param {@code <T>} is no {@code
         *   <R>
         * }
         */
        """.trimIndent() should parseAs {

            data("Param ")
            code("<T>")
            data(" is no ")
            code("<R>")
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
                it::getText shouldBe "<p> aha\n */"
                data(" aha")
            }
        }
    }

    parserTest("Test HTML char references") {

        """
/**
 *  <i> &amp; foo</i> <p> &#10; aha &#x00a;
 */
        """.trimIndent() should parseAs {

            html("i") {
                it::getText shouldBe "<i> &amp; foo</i>"
                data(" ")
                namedEntity("amp")
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it::getText shouldBe "<p> &#10; aha &#x00a;\n */"
                data(" ")
                decCharReference(10)
                data(" aha ")
                hexCharReference(10) {
                    it::getText shouldBe "&#x00a;"
                }
            }
        }

        // bad reference
        """
/** & amp; */
        """.trimIndent() should parseAs {
            data("& amp;")
        }
    }

    parserTest("Test void elements") {

        """
/**
 *  <area> contents <p> p;
 */
        """.trimIndent() should parseAs {

            html("area") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.VOID
            }
            data(" contents ")
            html("p") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT

                data(" p;")
            }
        }

    }

    parserTest("Test HTML attributes") {

        """
/**
 *  <a href="foo">
 */
        """.trim() should parseAs {

            html("a") {

                it::getText shouldBe "<a href=\"foo\">\n */"

                it.getAttribute("href") shouldBe htmlAttr("href", DOUBLE_QUOTED) {
                    it::getValue shouldBe "foo"

                    it::getText shouldBe "href=\"foo\""

                }
            }
        }

        """
/**
 *  <a href='foo'> </href>
 */
        """.trim() should parseAs {

            html("a") {

                it::getText shouldBe "<a href='foo'> </href>\n */"

                it.getAttribute("href") shouldBe htmlAttr("href", SINGLE_QUOTED) {

                    it::getText shouldBe "href='foo'"

                    it::getValue shouldBe "foo"
                }
                data(" ")
                htmlEnd("href")
            }
        }

        """
/**
 *  <a href=foo bar=oha > </a>
 */
        """.trim() should parseAs {

            html("a") {

                it::getText shouldBe "<a href=foo bar=oha > </a>"


                it.getAttribute("href") shouldBe htmlAttr("href", UNQUOTED) {
                    it::getText shouldBe "href=foo"

                    it::getValue shouldBe "foo"
                }

                it.getAttribute("bar") shouldBe htmlAttr("bar", UNQUOTED) {
                    it::getText shouldBe "bar=oha"

                    it::getValue shouldBe "oha"
                }

                data(" ")
                htmlEnd("a")

            }
        }

        """
/**
 *  <a href bar >
 */
        """.trim() should parseAs {

            html("a") {

                it.getAttribute("href") shouldBe htmlAttr("href", EMPTY) {
                    it::getText shouldBe "href"
                    it::getValue shouldBe "href"
                }

                it.getAttribute("bar") shouldBe htmlAttr("bar", EMPTY) {
                    it::getText shouldBe "bar"
                    it::getValue shouldBe "bar"
                }
            }
        }
    }

    parserTest("Autoclosing HTML") {

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
            }
            html("ul") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.HTML
                html("li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("LI one")
                }
                html("li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("LI two")
                    html("p") {
                        it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                        data("LIP ")
                        typeLink(name = "net.sourceforge.pmd.lang.java.ast.JavaNode")
                    }
                }
                html("li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.HTML
                    data("LI three")
                    htmlEnd("li")
                }
                htmlEnd("ul")
            }

        }


        """
/**
 *  contents <unknown> p;
 */
        """.trimIndent() should parseAs {

            data("contents ")
            html("unknown") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.UNCLOSED

                data(" p;")
            }
        }
        """
/**
 *  <ul><li>li1<li>li2
 */
        """.trimIndent() should parseAs {


            html("ul") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.UNCLOSED
                html("li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("li1")
                }
                html("li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("li2")
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

fun TreeNodeWrapper<Node, out JavadocNode>.htmlAttr(name: String,
                                                    syntax: HtmlAttrSyntax,
                                                    spec: NodeSpec<JdocHtmlAttr> = EmptyAssertions) =
        child<JdocHtmlAttr> {
            it::getName shouldBe name
            it::getSyntax shouldBe syntax
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

fun TreeNodeWrapper<Node, out JavadocNode>.code(data: String, spec: NodeSpec<JdocLiteral> = EmptyAssertions) =
        child<JdocLiteral> {
            it::getTagName shouldBe "@code"
            it::isLiteral shouldBe false
            it::isCode shouldBe true
            it::getData shouldBe data
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.literal(data: String, spec: NodeSpec<JdocLiteral> = EmptyAssertions) =
        child<JdocLiteral> {
            it::getTagName shouldBe "@literal"
            it::isLiteral shouldBe true
            it::isCode shouldBe false
            it::getData shouldBe data
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.namedEntity(name: String, spec: NodeSpec<JdocCharacterReference> = EmptyAssertions) =
        child<JdocCharacterReference> {
            it::getName shouldBe name
            it::getCodePoint shouldBe 0
            it::isHexadecimal shouldBe false
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.hexCharReference(point: Int, spec: NodeSpec<JdocCharacterReference> = EmptyAssertions) =
        child<JdocCharacterReference> {
            it::getName shouldBe null
            it::getCodePoint shouldBe point
            it::isHexadecimal shouldBe true
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.decCharReference(point: Int, spec: NodeSpec<JdocCharacterReference> = EmptyAssertions) =
        child<JdocCharacterReference> {
            it::getName shouldBe null
            it::getCodePoint shouldBe point
            it::isHexadecimal shouldBe false
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.typeLink(name: String, plain: Boolean = false, spec: NodeSpec<JdocLink> = EmptyAssertions) =
        link(plain) {
            it::getTypeName shouldBe name
            it::getFieldName shouldBe null
            it::getArgs shouldBe null
            spec()
        }


val JavadocNode.tokens: List<JavadocToken> get() = firstToken.rangeTo(lastToken).toList()
