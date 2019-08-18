/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal

import io.kotlintest.fail
import io.kotlintest.shouldBe
import net.sourceforge.pmd.lang.cfa.FlowGraph
import net.sourceforge.pmd.lang.java.ast.ASTBlock
import net.sourceforge.pmd.lang.java.ast.AstTestSpec
import net.sourceforge.pmd.lang.java.ast.JavaNode
import net.sourceforge.pmd.lang.java.ast.ParserTestCtx
import net.sourceforge.pmd.lang.java.cfa.CfgDisplayOpts
import net.sourceforge.pmd.lang.java.cfa.JavaCfa
import net.sourceforge.pmd.lang.java.cfa.JavaCfgTestRenderer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Paths


abstract class CfgTestSpec(private val resourcePrefix: String, val defaultTestOpts: CfgDisplayOpts = CfgDisplayOpts(), body: CfgTestSpec.() -> Unit) : AstTestSpec<CfgTestSpec>(body) {

    /**
     * @param ref Simple name w/o extension of the reference file
     * @param vars Variable map, occurrences of a key presented like: `{{{key}}}` will be replaced by value
     * @param testOpts Options the renderer used to generate the reference file
     */
    fun ParserTestCtx.cfgTest(ref: String, vars: Map<String, String> = emptyMap(), testOpts: CfgDisplayOpts = defaultTestOpts, code: () -> String) {

        fun String.normalize(): String =
                trimIndent().replace("\t", "    ")

        val block = parseStatement<ASTBlock>(code())
        val cfg = buildCfg(block)

        writeOut(cfg) // just for debugging

        JavaCfgTestRenderer(testOpts).renderString(cfg).normalize() shouldBe getTestFile(ref).interpolate(vars).normalize()
    }

    protected open fun buildCfg(block: ASTBlock): FlowGraph<JavaNode> = JavaCfa.builder.buildCfg(block)


    private fun writeOut(cfg: FlowGraph<JavaNode>) {
        try {
            val cfgDir = Paths.get(System.getProperty("user.home")).resolve(".pmd").resolve("cfg")
            JavaCfa.printers.gexf().renderToFile(cfg, cfgDir.resolve("cfg.gexf"))
            JavaCfa.printers.latex().renderToFile(cfg, cfgDir.resolve("cfg.tex"))
        } catch (e: IOException) {
            e.printStackTrace() // not to worry
        }
    }

    private fun getTestFile(name: String): String {
        val stream = this::class.java.getResourceAsStream("$resourcePrefix/$name.txt")

        return stream.readAllBytes().toString(Charset.defaultCharset()).also { stream.close() }
    }

    private fun String.interpolate(vars: Map<String, String>): String =
            INTERPOL_PATTERN.replace(this) {
                it.groups[1]?.let {
                    vars[it.value]
                } ?: fail("Wrong interpol pattern for $vars -- ${it.value}")
            }


    companion object {

        val INTERPOL_PATTERN = Regex("""\{\{\{(\w+)}}}""")

    }


}
