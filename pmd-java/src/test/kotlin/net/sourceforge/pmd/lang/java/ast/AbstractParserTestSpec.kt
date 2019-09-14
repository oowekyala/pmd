/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast

import io.kotlintest.*
import io.kotlintest.specs.IntelliMarker
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.RootNode
import net.sourceforge.pmd.lang.ast.test.*
import net.sourceforge.pmd.lang.java.ast.ParserTestCtx.Companion.findFirstNodeOnStraightLine
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

    abstract class VersionedTestCtx<V : Ver<V>, T : VersionedTestCtx<V, T>>(val spec: AbstractParserTestSpec<V, T>, private val context: KotlinTestRunCtx, val javaVersion: V) {


        /**
         * Parse the string the context described by this object, and finds the first descendant of type [N].
         * The descendant is searched for by [findFirstNodeOnStraightLine], to prevent accidental
         * mis-selection of a node. In such a case, a [NoSuchElementException] is thrown, and you
         * should fix your test case.
         *
         * @param construct The construct to parse
         * @param N The type of node to find
         *
         * @return The first descendant of type [N] found in the parsed expression
         *
         * @throws NoSuchElementException If no node of type [N] is found by [findFirstNodeOnStraightLine]
         * @throws ParseException If the argument is no valid construct of this kind
         *
         */
        inline fun <reified N : Node> NodeParsingCtx<*, T>.parseAndFind(construct: String): N =
                parseNode(construct, this as T).findFirstNodeOnStraightLine(N::class.java)
                        ?: throw NoSuchElementException("No node of type ${N::class.java.simpleName} in the given $this:\n\t$construct")

        infix fun <N : Node> NodeParsingCtx<N, T>.parse(construct: String): N = parseNode(construct, this as T)

        fun V.rootParsingCtx(): NodeParsingCtx<RootNode, T> = object : NodeParsingCtx<RootNode, T> {
            override fun parseNode(construct: String, ctx: T): RootNode = this@rootParsingCtx.parse(construct)
        }

        inline fun <reified N : JavaNode> makeMatcher(nodeParsingCtx: NodeParsingCtx<*, T>, ignoreChildren: Boolean, noinline nodeSpec: NodeSpec<N>)
                : Assertions<String> = { nodeParsingCtx.parseAndFind<N>(it).shouldMatchNode(ignoreChildren, nodeSpec) }

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
                    test = { (this@VersionedTestCtx as T).assertions() },
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
