package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AbstractFunSpec
import net.sourceforge.pmd.lang.LanguageRegistry
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.NWrapper
import net.sourceforge.pmd.lang.ast.test.getChild
import net.sourceforge.pmd.lang.ast.test.matchNode
import net.sourceforge.pmd.lang.ast.test.numChildren
import net.sourceforge.pmd.lang.xpath.XPathLanguageModule
import java.io.StringReader


/**
 * Represents the different Java language versions.
 */
enum class XPathVersion : Comparable<XPathVersion> {
    //X1_0, X2_0,
    X3_0;

    /** Name suitable for use with */
    val pmdName: String = name.removePrefix("X").replace('_', '.')

    /**
     * Overloads the range operator, e.g. (`J9..J11`).
     * If both operands are the same, a singleton list is returned.
     */
    operator fun rangeTo(last: XPathVersion): List<XPathVersion> =
            when {
                last == this -> listOf(this)
                last.ordinal > this.ordinal -> values().filter { ver -> ver >= this && ver <= last }
                else -> values().filter { ver -> ver <= this && ver >= last }
            }

    companion object {
        val Latest = values().last()
        val Earliest = values().first()
    }
}


/**
 * Specify several tests at once for different java versions.
 * One test will be generated per version in [xpathVersions].
 * Use [focusOn] to execute one test in isolation.
 *
 * @param name Name of the test. Will be postfixed by the specific
 *             java version used to run it
 * @param xpathVersions Language versions for which to generate tests
 * @param focusOn Sets the java version of the test to isolate
 * @param assertions Assertions and further configuration
 *                   to perform with the parsing context
 */
fun AbstractFunSpec.parserTest(name: String,
                               xpathVersions: List<XPathVersion>,
                               focusOn: XPathVersion? = null,
                               assertions: ParserTestCtx.() -> Unit) {

    xpathVersions.forEach {

        val focus = if (focusOn != null && focusOn == it) "f:" else ""

        test("$focus$name (XPath ${it.pmdName})") {
            ParserTestCtx(it).assertions()
        }
    }
}

/**
 * Specify a new test for a single java version. To execute the test in isolation,
 * prefix the name with `"f:"`.
 *
 * @param name Name of the test. Will be postfixed by the [xpathVersion]
 * @param xpathVersion Language version to use when parsing
 * @param assertions Assertions and further configuration
 *                   to perform with the parsing context
 */
fun AbstractFunSpec.parserTest(name: String,
                               xpathVersion: XPathVersion = XPathVersion.Latest,
                               assertions: ParserTestCtx.() -> Unit) {
    parserTest(name, listOf(xpathVersion), null, assertions)
}


data class ParserTestCtx(val xpathVersion: XPathVersion = XPathVersion.Latest) {

    /**
     * Returns a String matcher that parses the node using [parseXPath] with
     * type param [N], then matches it against the [nodeSpec] using [matchNode].
     *
     */
    inline fun <reified N : XPathNode> matchExpr(ignoreChildren: Boolean = false,
                                                 noinline nodeSpec: NWrapper<N>.() -> Unit): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result =
                        matchNode(ignoreChildren, nodeSpec).test(parseXPath<N>(value))
            }

    fun matchRoot(ignoreChildren: Boolean = false,
                  nodeSpec: NWrapper<ASTXPathRoot>.() -> Unit): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result =
                        matchNode(ignoreChildren, nodeSpec).test(parseXPath<ASTXPathRoot>(value))
            }


    /**
     * Expect a parse exception to be thrown by [block].
     * The message is asserted to contain [messageContains].
     */
    fun expectParseException(messageContains: String = "", block: () -> Unit) {

        val thrown = shouldThrow<ParseException>(block)

        thrown.message.shouldContain(messageContains)

    }

    private fun getLangVersionHandler(version: XPathVersion): LanguageVersionHandler =
            LanguageRegistry.getLanguage(XPathLanguageModule.NAME).getVersion(version.pmdName).languageVersionHandler


    fun parseXPathRoot(expr: String): ASTXPathRoot {
        val languageVersionHandler = getLangVersionHandler(xpathVersion)
        val rootNode = languageVersionHandler.getParser(languageVersionHandler.defaultParserOptions)
                .parse(":test:", StringReader(expr)) as ASTXPathRoot
        languageVersionHandler.getQualifiedNameResolutionFacade(ParserTestCtx::class.java.classLoader).start(rootNode)
        return rootNode
    }

    inline fun <reified N : XPathNode> parseXPath(expr: String): N =
            parseXPathRoot(expr).findFirstNodeOnStraightLine(N::class.java)
            ?: throw NoSuchElementException("No node of type ${N::class.java.simpleName} in the given expression:\n\t$expr")


    /**
     * Finds the first descendant of type [N] of [this] node which is
     * accessible in a straight line. The descendant must be accessible
     * from the [this] on a path where each node has a single child.
     *
     * If one node has another child, the search is aborted and the method
     * returns null.
     */
    fun <N : Node> Node.findFirstNodeOnStraightLine(klass: Class<N>): N? {
        return when {
            klass.isInstance(this) -> {
                @Suppress("UNCHECKED_CAST")
                val n = this as N
                n
            }
            this.numChildren == 1 -> getChild(0).findFirstNodeOnStraightLine(klass)
            else -> null
        }
    }


}

