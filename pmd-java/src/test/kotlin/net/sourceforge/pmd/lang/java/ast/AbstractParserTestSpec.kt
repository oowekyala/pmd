/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import io.kotlintest.*
import io.kotlintest.specs.IntelliMarker
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper
import net.sourceforge.pmd.lang.ast.test.ValuedNodeSpec
import net.sourceforge.pmd.lang.ast.test.matchNode
import io.kotlintest.TestContext as KotlinTestRunCtx
import io.kotlintest.should as kotlintestShould

/**
 * Base class for grammar tests that use the DSL. Tests are layered into
 * containers that make it easier to browse in the IDE. Layout is group name,
 * then java version, then test case. Test cases are "should" assertions matching
 * a string against a matcher defined in [ParserTestCtx], e.g. [ParserTestCtx.matchExpr].
 *
 * @author Clément Fournier
 */
abstract class AbstractParserTestSpec<V : Ver<V>, T : AbstractParserTestSpec.VersionedTestCtx<V, T>> : AbstractSpec(), IntelliMarker {

    protected abstract fun makeCtx(testCtx: KotlinTestRunCtx, ver: V): T

    protected abstract val defaultVer: V

    fun test(name: String, test: KotlinTestRunCtx.() -> Unit) =
            addTestCase(name, test, defaultTestCaseConfig, TestType.Test)

    /**
     * Defines a group of tests that should be named similarly,
     * with separate tests for separate versions.
     *
     * Calls to "should" in the block are intercepted to create
     * a new test, with the given [name] as a common prefix.
     *
     * This is useful to make a batch of grammar specs for grammar
     * regression tests without bothering to find a name.
     *
     * @param name Name of the container test
     * @param spec Assertions. Each call to [io.kotlintest.should] on a string
     *             receiver is replaced by a [GroupTestCtx.should], which creates a
     *             new parser test.
     *
     */
    fun parserTestGroup(name: String,
                        spec: GroupTestCtx.() -> Unit) =
            addTestCase(name, { GroupTestCtx(this).spec() }, defaultTestCaseConfig, TestType.Container)

    /**
     * Defines a group of tests that should be named similarly.
     * Calls to "should" in the block are intercepted to create
     * a new test, with the given [name] as a common prefix.
     *
     * This is useful to make a batch of grammar specs for grammar
     * regression tests without bothering to find a name.
     *
     * @param name Name of the container test
     * @param javaVersion Language versions to use when parsing
     * @param spec Assertions. Each call to [io.kotlintest.should] on a string
     *             receiver is replaced by a [GroupTestCtx.should], which creates a
     *             new parser test.
     *
     */
    fun parserTest(name: String,
                   javaVersion: V = defaultVer,
                   spec: T.() -> Unit) =
            parserTest(name, listOf(javaVersion), spec)

    /**
     * Defines a group of tests that should be named similarly,
     * executed on several java versions. Calls to "should" in
     * the block are intercepted to create a new test, with the
     * given [name] as a common prefix.
     *
     * This is useful to make a batch of grammar specs for grammar
     * regression tests without bothering to find a name.
     *
     * @param name Name of the container test
     * @param javaVersions Language versions for which to generate tests
     * @param spec Assertions. Each call to [io.kotlintest.should] on a string
     *             receiver is replaced by a [GroupTestCtx.should], which creates a
     *             new parser test.
     */
    fun parserTest(name: String,
                   javaVersions: List<V>,
                   spec: T.() -> Unit) =
            parserTestGroup(name) {
                onVersions(javaVersions) {
                    spec()
                }
            }

    inner class GroupTestCtx(private val context: KotlinTestRunCtx) {

        fun onVersions(javaVersions: List<V>, spec: T.() -> Unit) {
            javaVersions.forEach { javaVersion ->
                makeCtx(context, javaVersion).containedParserTestImpl(context, name = javaVersion.displayName, assertions = spec)
            }
        }
    }

    abstract class VersionedTestCtx<V : Ver<V>, Self : VersionedTestCtx<V, Self>>(val spec: AbstractParserTestSpec<V, Self>, private val context: KotlinTestRunCtx, val version: V) {

        abstract val parser: BaseParsingHelper<*, *>


        fun notParseIn(nodeParsingCtx: NodeParsingCtx<*, Self>, expected: (ParseException) -> Unit = {}): Assertions<String> = {
            val e = shouldThrow<ParseException> {
                nodeParsingCtx.parseNode(it, thisCtx)
            }
            expected(e)
        }

        fun parseIn(nodeParsingCtx: NodeParsingCtx<*, Self>) = object : Matcher<String> {

            override fun test(value: String): Result {
                val (pass, e) = try {
                    nodeParsingCtx.parseNode(value, thisCtx)
                    Pair(true, null)
                } catch (e: ParseException) {
                    Pair(false, e)
                }

                return Result(pass,
                        "Expected '$value' to parse in $nodeParsingCtx, got $e",
                        "Expected '$value' not to parse in ${nodeParsingCtx.toString().addArticle()}"
                )
            }
        }

        fun doTest(name: String, assertions: Self.() -> Unit) {
            containedParserTestImpl(context, name) {
                assertions()
            }
        }

        infix fun String.should(matcher: Assertions<String>) {
            containedParserTestImpl(context, "'$this'") {
                this@should kotlintestShould matcher
            }
        }

        infix fun String.should(matcher: Matcher<String>) {
            containedParserTestImpl(context, "'$this'") {
                this@should kotlintestShould matcher
            }
        }


        internal fun containedParserTestImpl(
                context: KotlinTestRunCtx,
                name: String,
                assertions: Self.() -> Unit) {

            context.registerTestCase(
                    name = name,
                    spec = spec,
                    test = { thisCtx.assertions() },
                    config = spec.defaultTestCaseConfig,
                    type = TestType.Test
            )
        }


        infix fun String.shouldNot(matcher: Matcher<String>) =
                should(matcher.invert())

        fun inContext(nodeParsingCtx: NodeParsingCtx<*, Self>, assertions: ImplicitNodeParsingCtx.() -> Unit) {
            ImplicitNodeParsingCtx(nodeParsingCtx).assertions()
        }

        val thisCtx : Self = this as Self

        inner class ImplicitNodeParsingCtx(private val nodeParsingCtx: NodeParsingCtx<*, Self>) {


            /**
             * A matcher that succeeds if the string parses correctly.
             */
            fun parse(): Matcher<String> = this@VersionedTestCtx.parseIn(nodeParsingCtx)

            /**
             * A matcher that succeeds if parsing throws a ParseException.
             */
            fun throwParseException(expected: (ParseException) -> Unit = {}): Assertions<String> =
                    this@VersionedTestCtx.notParseIn(nodeParsingCtx, expected)


            fun parseAs(matcher: ValuedNodeSpec<Node, Any>): Assertions<String> = { str ->
                val node = nodeParsingCtx.parseNode(str, thisCtx)
                val idx = node.indexInParent
                node.parent kotlintestShould matchNode<Node> {
                    if (idx > 0) {
                        unspecifiedChildren(idx)
                    }
                    matcher()
                    val left = it.numChildren - 1 - idx
                    if (left > 0) {
                        unspecifiedChildren(left)
                    }
                }
            }
        }
    }
}



interface Ver<T : Ver<T>> : Comparable<T> {

    val ordinal: Int

    val values: Array<T>

    val displayName: String

    val parser: BaseParsingHelper<*, *>

    operator fun not(): List<T> = values.toList() - (this as T)


    /**
     * Overloads the range operator, e.g. (`J9..J11`).
     * If both operands are the same, a singleton list is returned.
     */
    operator fun rangeTo(last: T): List<T> =
            when {
                last == this                -> listOf(this as T)
                last.ordinal > this.ordinal -> values.filter { ver -> ver >= (this as T) && ver <= last }
                else                        -> values.filter { ver -> ver <= (this as T) && ver >= last }
            }
}