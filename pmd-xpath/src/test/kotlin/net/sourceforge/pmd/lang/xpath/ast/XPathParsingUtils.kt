package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Failures
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.specs.AbstractFunSpec
import net.sourceforge.pmd.lang.LanguageRegistry
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.TokenMgrError
import net.sourceforge.pmd.lang.ast.test.NWrapper
import net.sourceforge.pmd.lang.ast.test.matchNode
import net.sourceforge.pmd.lang.xpath.XPathLanguageModule
import java.io.StringReader
import java.util.*
import kotlin.reflect.KClass
import io.kotlintest.should as kotlintestShould


/**
 * Represents the different XPath language versions.
 */
enum class XPathVersion : Comparable<XPathVersion> {
    //X1_0, X2_0,
    X3_0;

    /** Name suitable for use with the language version handler. */
    val pmdName: String = name.removePrefix("X").replace('_', '.')

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

inline fun <reified M : Node> NWrapper<*>.childOpt(ignoreChildren: Boolean = false, noinline nodeSpec: NWrapper<M>.() -> Unit): Optional<M> {
    return childRet<M, Optional<M>> {
        nodeSpec()
        Optional.of(it)
    }
}


/**
 * Defines a group of tests that should be named similarly.
 * Calls to "should" in the block are intercepted to create
 * a new test, with the given [name] as a common prefix.
 */
fun AbstractFunSpec.testGroup(name: String,
                              xpathVersion: XPathVersion = XPathVersion.Latest,
                              spec: GroupTestCtx.() -> Unit) {

    GroupTestCtx(this, name, xpathVersion).spec()
}

class GroupTestCtx(val funspec: AbstractFunSpec, val groupName: String, xpathVersion: XPathVersion) : ParserTestCtx(xpathVersion) {

    infix fun String.should(matcher: Matcher<String>) {
        funspec.parserTest("$groupName: '$this'") {
            this@should kotlintestShould matcher
        }
    }

    /**
     * Create a new test for the matcher and give it focus.
     */
    infix fun String.SHOULD(matcher: Matcher<String>) {
        funspec.parserTest("f:$groupName: '$this'") {
            this@SHOULD kotlintestShould matcher
        }
    }

}

/**
 * Catches any parser error and handles it with the given handler. Other exceptions are thrown.
 *
 * @param handler exception handler called on a [ParseException] or [TokenMgrError]
 */
inline infix fun <T> (() -> T).catchAnyParserError(handler: (Exception) -> T): T =
        try {
            this()
        } catch (e: Exception) {
            val ex = when (e) {
                is ParseException -> e
                is TokenMgrError -> e
                else -> throw e
            }
            handler(ex)
        }

open class ParserTestCtx(private val xpathVersion: XPathVersion = XPathVersion.Latest) {

    /**
     * Returns a String matcher that matches the [nodeSpec] against the node right under the root using [matchNode].
     *
     */
    inline fun <reified N : XPathNode> matchExpr(ignoreChildren: Boolean = false,
                                                 noinline nodeSpec: NWrapper<N>.() -> Unit): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result {

                    return matchNode(ignoreChildren, nodeSpec).test(parseXPathRoot(value).jjtGetChild(0))
                }
            }

    fun throwParseFailure(): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result {
                    val message = try {
                        parseXPathRoot(value)
                        "Expected parser failure but no exception was thrown"
                    } catch (e: Throwable) {
                        when (e) {
                            is ParseException -> null
                            is TokenMgrError -> null
                            is AssertionError -> e.message
                            else -> "Expected parser failure but ${e.javaClass.name} was thrown"
                        }
                    }

                    return Result(message == null, message ?: "NOTNULL", "TODO")
                }
            }

    /**
     * Used to expect an exception when parsing a snippet of code. Use as
     *
     *     expect<ParseException>() whenParsing {
     *          "/foo/a/"
     *     }
     *
     * @param withMessage The message will be asserted to contain this string
     *
     * @return An object on which to call [ExpectSignal.whenParsing].
     */
    inline fun <reified T : Throwable> expect(withMessage: String = ""): ExpectSignal<T> = ExpectSignal(T::class, withMessage)


    inner class ExpectSignal<T : Throwable>(private val tClass: KClass<T>, private val messageContains: String) {
        /**
         * End of the sentence starting with [expect].
         * @param expr Block returning the snippet of code to parse
         *
         * @return The thrown exception if it is found
         */
        infix fun whenParsing(expr: () -> String): T {

            try {
                parseXPathRoot(expr())
                throw Failures.failure("Expected exception ${tClass.qualifiedName} but no exception was thrown")
            } catch (e: Throwable) {
                when {
                    tClass.java.isAssignableFrom(e.javaClass) -> {
                        e.message.shouldContain(messageContains)
                        @Suppress("UNCHECKED_CAST")
                        return e as T
                    }
                    e is AssertionError -> throw e
                    else -> throw Failures.failure("Expected exception ${tClass.qualifiedName} but ${e.javaClass.name} was thrown", e)
                }
            }
        }
    }


    private fun getLangVersionHandler(version: XPathVersion): LanguageVersionHandler =
            LanguageRegistry.getLanguage(XPathLanguageModule.NAME).getVersion(version.pmdName).languageVersionHandler


    fun parseXPathRoot(expr: String): ASTXPathRoot {
        val lvh = getLangVersionHandler(xpathVersion)
        val rootNode = lvh.getParser(lvh.defaultParserOptions).parse(":test:", StringReader(expr)) as ASTXPathRoot
        lvh.symbolFacade.start(rootNode)
        return rootNode
    }
}

