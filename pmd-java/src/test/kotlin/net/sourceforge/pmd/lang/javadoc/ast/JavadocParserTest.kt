/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.EmptyAssertions
import net.sourceforge.pmd.lang.java.ast.JavaMatchingConfig
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.*
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml.HtmlCloseSyntax
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.*
import net.sourceforge.pmd.lang.javadoc.ast.JdocInlineTag.*
import kotlin.streams.toList


class JavadocParserTest : JavadocParserSpec({
    /*
        TODO tests:
         - html comments
         - method reference
         - block tags
    */

    parserTest("Test @link inline tags") {


        """
        /**
         * See {@link #hey}
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link #hey}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link #hey}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "#hey"
                    classRef("") {
                        it::getText shouldBe ""
                        it::isImplicit shouldBe true
                    }
                }
            }
        }


        """
        /**
         * See {@link Oha#hey label label}
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link Oha#hey label label}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link Oha#hey label label}"
                it::getRef shouldBe fieldRef("hey") {
                    it::getText shouldBe "Oha#hey"
                    classRef("Oha") {
                        it::getText shouldBe "Oha"
                        it::isImplicit shouldBe false
                    }
                }

                it::getLabel shouldBe "label label"
                data("label label")
            }
        }

        // malformed

        """
        /**
         * See {@link Oha# label label}
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link Oha# label label}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link Oha# label label}"
                it::getRef shouldBe classRef("Oha") {
                    it::getText shouldBe "Oha#"
                    it::isImplicit shouldBe false

                    malformed {
                        it.message.shouldContain("Unexpected token #")
                    }
                }

                it::getLabel shouldBe "label label"
                data("label label")
            }
        }

        """
        /**
         * See {@link }
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link }\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link }"
                // TODO malformed?
                it::getRef shouldBe null
                it::getLabel shouldBe null
            }
        }

        """
        /**
         * See {@link}
         */
        """.trimIndent() should parseAs {
            it::getText shouldBe "/**\n * See {@link}\n */"

            data("See ")
            link {
                it::getText shouldBe "{@link}"
                // TODO malformed?
                it::getRef shouldBe null
                it::getLabel shouldBe null
            }
        }


    }


    parserTest("Test some inline tags") {


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


    parserTest("Test unknown inline tags") {

        """
        /**
         * See {@cobalt #hey}
         */
        """.trimIndent() should parseAs {
            data("See ")
            unknownInline("@cobalt") {
                it::getText shouldBe "{@cobalt #hey}"
                it::getData shouldBe "#hey"
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
                it::getText shouldBe "<p> aha\n */"
                data(" aha")
            }
        }
    }

    parserTest("Test HTML char references") {

        """
/**
 *  <i> &amp; foo</i> <p> &#160; aha &#x00a0;
 */
        """.trimIndent() should parseAs {

            html("i") {
                it::getText shouldBe "<i> &amp; foo</i>"
                data(" ")
                namedEntity("amp") {
                    it::getConstant shouldBe KnownHtmlEntity.AMP
                }
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it::getText shouldBe "<p> &#160; aha &#x00a0;\n */"
                data(" ")
                decCharReference(160) {
                    it::getConstant shouldBe KnownHtmlEntity.NONBREAKINGSPACE
                }
                data(" aha ")
                hexCharReference(160) {
                    it::getText shouldBe "&#x00a0;"
                    it::getConstant shouldBe KnownHtmlEntity.NONBREAKINGSPACE
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
 *  contents <ul><unknown> p;</ul>
 */
        """.trimIndent() should parseAs {

            data("contents ")
            html("ul") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.HTML
                html("unknown") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.UNCLOSED

                    data(" p;")
                }
                htmlEnd("ul")
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

    parserTest("Test case insensitivity") {

        """
/**
 * Header.
 *
 * <P>OHA
 * <UL>
 *     <LI>LI one
 *     <Li>LI two
 *     <P>LIP {@link net.sourceforge.pmd.lang.java.ast.JavaNode}
 *     <LI>LI three
 *     </li>
 * </uL>
 *
 */
""".trimIndent() should parseAs {
            data("Header.")

            html("P") {
                data("OHA")
            }
            html("UL") {
                it::getCloseSyntax shouldBe HtmlCloseSyntax.HTML
                html("LI") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("LI one")
                }
                html("Li") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                    data("LI two")
                    html("P") {
                        it::getCloseSyntax shouldBe HtmlCloseSyntax.IMPLICIT
                        data("LIP ")
                        typeLink(name = "net.sourceforge.pmd.lang.java.ast.JavaNode")
                    }
                }
                html("LI") {
                    it::getCloseSyntax shouldBe HtmlCloseSyntax.HTML
                    data("LI three")
                    htmlEnd("li")
                }
                htmlEnd("uL")
            }
        }
    }

})


fun JdocComment?.shouldMatchComment(spec: NodeSpec<JdocComment>) =
        this.baseShouldMatchSubtree<Node, JdocComment>(JavaMatchingConfig, false) {
            spec()
        }

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

fun TreeNodeWrapper<Node, out JavadocNode>.classRef(name: String, spec: NodeSpec<JdocClassRef> = EmptyAssertions) =
        child<JdocClassRef> {
            it::getSimpleRef shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.fieldRef(name: String, spec: NodeSpec<JdocFieldRef> = EmptyAssertions) =
        child<JdocFieldRef> {
            it::getName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.unknownInline(name: String, spec: NodeSpec<JdocUnknownInlineTag> = EmptyAssertions) =
        child<JdocUnknownInlineTag> {
            it::getTagName shouldBe name
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
            it::getRef shouldBe classRef(name)
            spec()
        }


val JavadocNode.tokens: List<JdocToken> get() = firstToken.rangeTo(lastToken).toList()
