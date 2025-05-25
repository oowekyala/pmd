/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.matchers.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldHaveText
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtml.HtmlCloseSyntax
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlAttr.HtmlAttrSyntax.*


class JdocHtmlParserTests : JdocParserTestSpec({
    /*
        TODO tests:
         - html comments
         - method reference
         - block tags
    */

    parserTest("Test some HTML") {

        """
/**
 *  <i> foo</i> <p> aha
 */
        """.trimIndent() should parseAsJdoc {

            html("i") {
                it shouldHaveText "<i> foo</i>" 
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it shouldHaveText "<p> aha\n */" 
                data(" aha")
            }
        }
    }

    parserTest("Test HTML char references") {

        """
/**
 *  <i> &amp; foo</i> <p> &#160; aha &#x00a0;
 */
        """.trimIndent() should parseAsJdoc {

            html("i") {
                it shouldHaveText "<i> &amp; foo</i>" 
                data(" ")
                namedEntity("amp") {
                    it::getConstant shouldBe KnownHtmlEntity.AMP
                }
                data(" foo")
                htmlEnd("i")
            }
            data(" ")
            html("p") {
                it shouldHaveText "<p> &#160; aha &#x00a0;\n */" 
                data(" ")
                decCharReference(160) {
                    it::getConstant shouldBe KnownHtmlEntity.NONBREAKINGSPACE
                }
                data(" aha ")
                hexCharReference(160) {
                    it shouldHaveText "&#x00a0;" 
                    it::getConstant shouldBe KnownHtmlEntity.NONBREAKINGSPACE
                }
            }
        }

        // bad reference
        """
/** & amp; */
        """.trimIndent() should parseAsJdoc {
            data("& amp;")
        }
    }

    parserTest("Test void elements") {

        """
/**
 *  <area> contents <p> p;
 */
        """.trimIndent() should parseAsJdoc {

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
        """.trim() should parseAsJdoc {

            html("a") {

                it shouldHaveText "<a href=\"foo\">\n */" 

                it.getAttribute("href") shouldBe htmlAttr("href", DOUBLE_QUOTED) {
                    it::getValue shouldBe "foo"

                    it shouldHaveText "href=\"foo\"" 

                }
            }
        }

        """
/**
 *  <a href='foo'> </href>
 */
        """.trim() should parseAsJdoc {

            html("a") {

                it shouldHaveText "<a href='foo'> </href>\n */" 

                it.getAttribute("href") shouldBe htmlAttr("href", SINGLE_QUOTED) {

                    it shouldHaveText "href='foo'" 

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
        """.trim() should parseAsJdoc {

            html("a") {

                it shouldHaveText "<a href=foo bar=oha > </a>" 


                it.getAttribute("href") shouldBe htmlAttr("href", UNQUOTED) {
                    it shouldHaveText "href=foo" 

                    it::getValue shouldBe "foo"
                }

                it.getAttribute("bar") shouldBe htmlAttr("bar", UNQUOTED) {
                    it shouldHaveText "bar=oha" 

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
        """.trim() should parseAsJdoc {

            html("a") {

                it.getAttribute("href") shouldBe htmlAttr("href", EMPTY) {
                    it shouldHaveText "href" 
                    it::getValue shouldBe "href"
                }

                it.getAttribute("bar") shouldBe htmlAttr("bar", EMPTY) {
                    it shouldHaveText "bar" 
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
""".trimIndent() should parseAsJdoc {
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
        """.trimIndent() should parseAsJdoc {

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
        """.trimIndent() should parseAsJdoc {

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
        """.trimIndent() should parseAsJdoc {


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
""".trimIndent() should parseAsJdoc {
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
