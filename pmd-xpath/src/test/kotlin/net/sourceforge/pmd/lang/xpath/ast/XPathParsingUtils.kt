package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.Failures
import io.kotlintest.matchers.string.shouldContain
import net.sourceforge.pmd.lang.LanguageRegistry
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.ast.TokenMgrError
import net.sourceforge.pmd.lang.ast.test.Assertions
import net.sourceforge.pmd.lang.ast.test.NodeSpec
import net.sourceforge.pmd.lang.ast.test.matchNode
import net.sourceforge.pmd.lang.ast.test.shouldMatchNode
import net.sourceforge.pmd.lang.xpath.XPathLanguageModule
import java.io.StringReader
import kotlin.reflect.KClass

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
                is TokenMgrError  -> e
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
                                                 noinline nodeSpec: NodeSpec<N>): Assertions<String> =
            { parseXPathRoot(it).lastChild.shouldMatchNode(ignoreChildren, nodeSpec) }

    fun throwParseFailure(): Assertions<String> =
            {
                try {
                    parseXPathRoot(it)
                    throw AssertionError("Expected parser failure but no exception was thrown")
                    } catch (e: Throwable) {
                        when (e) {
                            is ParseException, is TokenMgrError -> {
                            }
                            else                                -> throw AssertionError("Expected parser failure but ${e.javaClass.name} was thrown")
                        }
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

