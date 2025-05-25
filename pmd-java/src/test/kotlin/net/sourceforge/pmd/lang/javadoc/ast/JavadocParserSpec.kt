/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import io.kotlintest.TestContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.RootNode
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.ValuedNodeSpec
import net.sourceforge.pmd.lang.ast.test.shouldMatchNode
import net.sourceforge.pmd.lang.java.ast.AbstractParserTestSpec
import net.sourceforge.pmd.lang.java.ast.JavaMatchingConfig
import net.sourceforge.pmd.lang.java.ast.Ver


abstract class JavadocParserSpec(body: JavadocParserSpec.() -> Unit) : AbstractParserTestSpec<JavadocVer, JdocParserTestCtx>() {

    init {
        body()
    }

    override fun makeCtx(testCtx: TestContext, ver: JavadocVer): JdocParserTestCtx =
            JdocParserTestCtx(this, testCtx, ver)

    override val defaultVer: JavadocVer get() = JavadocVer.JAVADOC
}

class JdocParserTestCtx(spec: JavadocParserSpec, ktCtx: TestContext, version: JavadocVer) : AbstractParserTestSpec.VersionedTestCtx<JavadocVer, JdocParserTestCtx>(spec, ktCtx, version) {

    fun parseAs(matcher: ValuedNodeSpec<JavadocNode.JdocComment, Any>): Assertions<String> = {
        JavadocVer.JAVADOC.parse(it).baseShouldMatchSubtree<Node, JavadocNode.JdocComment>(JavaMatchingConfig, false) {
            matcher()
        }
    }

}


enum class JavadocVer : Ver<JavadocVer> {
    JAVADOC;

    override val values: Array<JavadocVer> get() = values()
    override val displayName: String get() = "Javadoc"

    override fun parse(code: String): RootNode = JavadocParser(code).parse()

}
