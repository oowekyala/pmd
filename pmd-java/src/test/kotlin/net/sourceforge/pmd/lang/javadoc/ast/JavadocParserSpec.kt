/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import io.kotlintest.TestContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.RootNode
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper
import net.sourceforge.pmd.lang.ast.test.ValuedNodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.java.ast.AbstractParserTestSpec
import net.sourceforge.pmd.lang.java.ast.EmptyAssertions
import net.sourceforge.pmd.lang.java.ast.JavaMatchingConfig
import net.sourceforge.pmd.lang.java.ast.Ver
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment
import kotlin.streams.toList


abstract class JavadocParserSpec(body: JavadocParserSpec.() -> Unit) : AbstractParserTestSpec<JavadocVer, JdocParserTestCtx>() {

    init {
        body()
    }

    override fun makeCtx(testCtx: TestContext, ver: JavadocVer): JdocParserTestCtx =
            JdocParserTestCtx(this, testCtx, ver)

    override val defaultVer: JavadocVer get() = JavadocVer.JAVADOC
}

class JdocParserTestCtx(spec: JavadocParserSpec, ktCtx: TestContext, version: JavadocVer) : AbstractParserTestSpec.VersionedTestCtx<JavadocVer, JdocParserTestCtx>(spec, ktCtx, version) {

    override val parser: JavadocParsingHelper
        get() = version.parser

    fun parseAs(matcher: ValuedNodeSpec<JdocComment, Any>): Assertions<String> = {
        version.parser.parse(it).shouldMatchComment {
            matcher()
        }
    }

}


enum class JavadocVer : Ver<JavadocVer> {
    JAVADOC;

    override val parser: JavadocParsingHelper
        get() = JavadocParsingHelper.Default

    override val values: Array<JavadocVer> get() = values()
    override val displayName: String get() = "Javadoc"

}

class JavadocParsingHelper(params: Params)
    : BaseParsingHelper<JavadocParsingHelper, JdocComment>("Javadoc", JdocComment::class.java, params) {

    override fun clone(params: Params): JavadocParsingHelper = JavadocParsingHelper(params)


    override fun parse(sourceCode: String, version: String?): JdocComment =
            MainJdocParser(JavadocLexer(sourceCode)).parse()


    companion object {

        val Default = JavadocParsingHelper(Params.defaultProcess)

    }

}



fun JavadocNode.JdocComment?.shouldMatchComment(spec: NodeSpec<JavadocNode.JdocComment>) =
        this.baseShouldMatchSubtree<Node, JavadocNode.JdocComment>(JavaMatchingConfig, false) {
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
            it::getData shouldBe data
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
            it::getText shouldBe ""
            it::isImplicit shouldBe true
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.fieldRef(name: String, spec: NodeSpec<JdocRef.JdocFieldRef> = EmptyAssertions) =
        child<JdocRef.JdocFieldRef> {
            it::getName shouldBe name
            spec()
        }

fun TreeNodeWrapper<Node, out JavadocNode>.unknownInline(name: String, spec: NodeSpec<JdocInlineTag.JdocUnknownInlineTag> = EmptyAssertions) =
        child<JdocInlineTag.JdocUnknownInlineTag> {
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


val JavadocNode.tokens: List<JdocToken> get() = firstToken.rangeTo(lastToken).toList()
