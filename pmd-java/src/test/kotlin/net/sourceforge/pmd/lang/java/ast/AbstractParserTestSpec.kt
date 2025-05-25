/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import io.kotlintest.*
import io.kotlintest.specs.IntelliMarker
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.*
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

    open abstract class VersionedTestCtx<V : Ver<V>, T : VersionedTestCtx<V, T>>(val spec: AbstractParserTestSpec<V, T>, private val context: KotlinTestRunCtx, val javaVersion: V) {


        protected inline fun <reified N : JavaNode> makeMatcher(nodeParsingCtx: NodeParsingCtx<*, T>, ignoreChildren: Boolean, noinline nodeSpec: NodeSpec<N>)
                : Assertions<String> = { nodeParsingCtx.parseAndFind<N>(it, this as T).shouldMatchNode(ignoreChildren, nodeSpec) }

        fun notParseIn(nodeParsingCtx: NodeParsingCtx<*, T>): Assertions<String> = {
            shouldThrow<ParseException> {
                nodeParsingCtx.parseNode(it, this as T)
            }
        }

        fun parseIn(nodeParsingCtx: NodeParsingCtx<*, T>) = object : Matcher<String> {

            override fun test(value: String): Result {
                val (pass, e) = try {
                    nodeParsingCtx.parseNode(value, this as T)
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
                assertions: T.() -> Unit) {

            context.registerTestCase(
                    name = name,
                    spec = spec,
                    test = { (this as T).assertions() },
                    config = spec.defaultTestCaseConfig,
                    type = TestType.Test
            )
        }


        infix fun String.shouldNot(matcher: Matcher<String>) =
                should(matcher.invert())

        fun inContext(nodeParsingCtx: NodeParsingCtx<*, T>, assertions: ImplicitNodeParsingCtx.() -> Unit) {
            ImplicitNodeParsingCtx(nodeParsingCtx).assertions()
        }

        inner class ImplicitNodeParsingCtx(private val nodeParsingCtx: NodeParsingCtx<*, T>) {

            /**
             * A matcher that succeeds if the string parses correctly.
             */
            fun parse(): Matcher<String> = parseIn(nodeParsingCtx)

            fun parseAs(matcher: ValuedNodeSpec<Node, Any>): Assertions<String> = { str ->
                val node = nodeParsingCtx.parseNode(str, this@VersionedTestCtx as T)
                val idx = node.jjtGetChildIndex()
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
