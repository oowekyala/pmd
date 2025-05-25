/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import io.kotest.matchers.shouldBe
import net.sourceforge.pmd.internal.util.IteratorUtil
import net.sourceforge.pmd.lang.ast.GenericToken
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.textStr
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.ast.ParserTestSpec.GroupTestCtx.VersionedTestCtx

abstract class JdocParserTestSpec(body: ParserTestSpec.() -> Unit)
    : ProcessorTestSpec(body)


fun VersionedTestCtx.parseAsJdoc(matcher: NodeSpec<JavadocNode.JdocComment>)
        : (String) -> Unit =
        { str ->
            JavadocParsingCtx.parseNode(str, this).shouldMatchComment(matcher)
        }


fun JavadocNode.JdocComment?.shouldMatchComment(spec: NodeSpec<JavadocNode.JdocComment>) =
        this.baseShouldMatchSubtree<Node, JavadocNode.JdocComment>(JavaMatchingConfig, false) {
            spec()
        }

fun TreeNodeWrapper<Node, *>.jdoc(spec: NodeSpec<JavadocNode.JdocComment>) =
        child<JavadocNode.JdocComment> {
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.html(name: String, spec: NodeSpec<JavadocNode.JdocHtml>) =
        child<JavadocNode.JdocHtml> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.htmlEnd(name: String, spec: NodeSpec<JavadocNode.JdocHtmlEnd> = EmptyAssertions) =
        child<JavadocNode.JdocHtmlEnd> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.htmlAttr(name: String,
                                                    syntax: JavadocNode.JdocHtmlAttr.HtmlAttrSyntax,
                                                    spec: NodeSpec<JavadocNode.JdocHtmlAttr> = EmptyAssertions) =
        child<JavadocNode.JdocHtmlAttr> {
            it::getName shouldBe name
            it::getSyntax shouldBe syntax
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.malformed(spec: NodeSpec<JavadocNode.JdocMalformed>) =
        child<JavadocNode.JdocMalformed> {
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.data(data: String, spec: NodeSpec<JavadocNode.JdocCommentData> = EmptyAssertions) =
        child<JavadocNode.JdocCommentData> {
            it.data.toString() shouldBe data
            spec()
        }


fun TreeNodeWrapper<Node, out JavadocNode>.link(plain: Boolean = false, spec: NodeSpec<JdocInlineTag.JdocLink> = EmptyAssertions) =
        child<JdocInlineTag.JdocLink> {
            it::getTagName shouldBe if (plain) "@linkplain" else "@link"
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.value(spec: NodeSpec<JdocInlineTag.JdocValue> = EmptyAssertions) =
        child<JdocInlineTag.JdocValue> {
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.classRef(name: String, spec: NodeSpec<JdocRef.JdocClassRef> = EmptyAssertions) =
        child<JdocRef.JdocClassRef> {
            it::getSimpleRef shouldBe name
            it::isImplicit shouldBe false
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.emptyClassRef(spec: NodeSpec<JdocRef.JdocClassRef> = EmptyAssertions) =
        child<JdocRef.JdocClassRef> {
            it::getSimpleRef shouldBe ""
            it.textStr shouldBe ""
            it::isImplicit shouldBe true
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.fieldRef(name: String, spec: NodeSpec<JdocRef.JdocFieldRef> = EmptyAssertions) =
        child<JdocRef.JdocFieldRef> {
            it::getName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.jdocMethodRef(name: String, spec: NodeSpec<JdocRef.JdocExecutableRef> = EmptyAssertions) =
        child<JdocRef.JdocExecutableRef> {
            it::getName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.unknownInline(name: String, spec: NodeSpec<JdocInlineTag.JdocUnknownInlineTag> = EmptyAssertions) =
        child<JdocInlineTag.JdocUnknownInlineTag> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.blockTag(name: String, spec: NodeSpec<JdocBlockTag> = EmptyAssertions) =
        child<JdocBlockTag> {
            it::getTagName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.code(data: String, spec: NodeSpec<JdocInlineTag.JdocLiteral> = EmptyAssertions) =
        child<JdocInlineTag.JdocLiteral> {
            it::getTagName shouldBe "@code"
            it::isLiteral shouldBe false
            it::isCode shouldBe true
            it::getData shouldBe data
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.literal(data: String, spec: NodeSpec<JdocInlineTag.JdocLiteral> = EmptyAssertions) =
        child<JdocInlineTag.JdocLiteral> {
            it::getTagName shouldBe "@literal"
            it::isLiteral shouldBe true
            it::isCode shouldBe false
            it::getData shouldBe data
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.namedEntity(name: String, spec: NodeSpec<JavadocNode.JdocCharacterReference> = EmptyAssertions) =
        child<JavadocNode.JdocCharacterReference> {
            it::getName shouldBe name
            it::getCodePoint shouldBe 0
            it::isHexadecimal shouldBe false
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.hexCharReference(point: Int, spec: NodeSpec<JavadocNode.JdocCharacterReference> = EmptyAssertions) =
        child<JavadocNode.JdocCharacterReference> {
            it::getName shouldBe null
            it::getCodePoint shouldBe point
            it::isHexadecimal shouldBe true
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.decCharReference(point: Int, spec: NodeSpec<JavadocNode.JdocCharacterReference> = EmptyAssertions) =
        child<JavadocNode.JdocCharacterReference> {
            it::getName shouldBe null
            it::getCodePoint shouldBe point
            it::isHexadecimal shouldBe false
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.typeLink(name: String, plain: Boolean = false, spec: NodeSpec<JdocInlineTag.JdocLink> = EmptyAssertions) =
        link(plain) {
            it::getRef shouldBe classRef(name)
            spec()
        }


val JavadocNode.tokens: List<JdocToken>
    get() = GenericToken.range(firstToken, lastToken).toList()
