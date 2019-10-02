/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotlintest.TestContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.RootNode
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper
import net.sourceforge.pmd.lang.ast.test.ValuedNodeSpec
import net.sourceforge.pmd.lang.java.ast.AbstractParserTestSpec
import net.sourceforge.pmd.lang.java.ast.Ver
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment


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
            JavadocParser(JavadocLexer(sourceCode)).parse()


    companion object {

        val Default = JavadocParsingHelper(Params.defaultProcess)

    }

}
