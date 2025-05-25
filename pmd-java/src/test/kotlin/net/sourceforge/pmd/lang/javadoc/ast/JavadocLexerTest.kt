/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.ast.TokenMgrError
import net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.*
import org.assertj.core.util.diff.DiffUtils
import org.junit.ComparisonFailure
import java.io.EOFException
import java.io.IOException
import kotlin.test.assertEquals


class JavadocLexerTest : FunSpec({


    test("Test out of bounds max offset gives EOF") {

        val code = "01234567/** some javadoc */"
        //                  ^

        val lexer = JavadocLexer(code, 8, code.length + 10)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 24, image = "some javadoc")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 24, end = 25, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 25, end = 27, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at COMMENT_END") {

        val comment = "/** some javadoc */"
        val code = "01234567${comment}public void foo()"
        //                  ^
        val lexer = JavadocLexer(code, 8, 8 + comment.length + 3)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 24, image = "some javadoc")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 24, end = 25, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 25, end = 27, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at EOF") {

        val code = "01234567/** some javadoc "
        //                  ^
        val lexer = JavadocLexer(code, 8, 100)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 25, image = "some javadoc ")
        lexer.nextToken shouldBe null
    }



    test("Test java unicode escapes") {

        val comment = """\u002F\u002a\u002a\u002a\u002F"""
        val opening = """\u002F\u002a\u002a"""

        val lexer = JavadocLexer(comment)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = opening.length, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = opening.length, end = comment.length, image = "*/")
        lexer.nextToken shouldBe null

    }


    test("Test java escaped unicode escapes") {

        val comment = """\u002F\u002a\u002a\\u002a\u002F"""
        val opening = """\u002F\u002a\u002a"""

        val lexer = JavadocLexer(comment)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = opening.length, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = opening.length, end = comment.length, image = "\\\\u002a/")
        lexer.nextToken shouldBe null

    }

    test("Test java invalid unicode escapes") {

        val comment = """\u002F\u0k2a\u002a\u002a\u002F"""

        val lexer = JavadocLexer(comment)

        val tmgrError = shouldThrow<TokenMgrError> {
            lexer.nextToken
        }

        val ioe = tmgrError.cause!!

        ioe.shouldBeInstanceOf<IOException>()

        ioe.message!!.shouldContain(Regex("line \\d+, column \\d+"))
        ioe.message!!.shouldContain("\\u0k2a")

        ioe.cause.shouldBeInstanceOf<NumberFormatException>()

        ioe.cause!!.message!!.shouldContain("valid hexadecimal digit")
    }

    test("Test incomplete unicode escape ") {

        val comment = """\u00"""

        val lexer = JavadocLexer(comment)

        val tmgrError = shouldThrow<TokenMgrError> {
            lexer.nextToken
        }

        val ioe = tmgrError.cause!!

        ioe.shouldBeInstanceOf<IOException>()

        ioe.message!!.shouldContain(Regex("line \\d+, column \\d+"))
        ioe.message!!.shouldContain("\\u00")

        ioe.cause.shouldBeInstanceOf<EOFException>()
    }

    test("Test brace balancing") {

        // the <p> is interpreted as COMMENT_DATA inside the {@code}, but as HTML outside
        """/** some javadoc 
                <pre>{@code { <p> } } <p> </pre> */
            """.shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "some javadoc"),
                Tok(WHITESPACE, " "),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, "                "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                Tok(WHITESPACE, " "),
                // here's it's comment data
                Tok(COMMENT_DATA, "{ <p> } "),
                Tok(INLINE_TAG_END),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "p"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LCLOSE),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }
    test("Test line breaks") {

        """
/**
 * @param fileText    Full file text
 * @param startOffset Start offset in the file text
 */
""".trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "fileText    Full file text"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "startOffset Start offset in the file text"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }

    test("Test trailing whitespace") {

        """
/**
 * abc   
 *    ^^^
 *    those are spaces
 */
""".trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "abc"),
                Tok(WHITESPACE, "   "),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, "    "),
                Tok(COMMENT_DATA, "^^^"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, "    "),
                Tok(COMMENT_DATA, "those are spaces"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }

    test("Test param") {

        """
/**
 * @param fileText
 *          Full file text
 * @param startOffset
 *          Start offset in the file text
 */
""".trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "fileText"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, "          "),
                Tok(COMMENT_DATA, "Full file text"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "startOffset"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, "          "),
                Tok(COMMENT_DATA, "Start offset in the file text"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }

    test("Test html attributes empty syntax") {

        """
/** <value foo> */
""".trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "value"),
                Tok(WHITESPACE, " "),
                Tok(HTML_IDENT, "foo"),
                Tok(HTML_GT),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }

    test("Test block tag is interpreted in @code 1") {


        """
/**
 * {@code
 * foof
 * @param fullText
 * }
 */

        """.trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "foof"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "fullText"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "}"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }

    test("Test @code 3") {


        """
/**
 * {@code
 * foof
 * @ param fullText
 * }
 */

        """.trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "foof"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "@ param fullText"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_END, "}"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }
    test("Test @code brace matching") {


        """
/**
 * {@code {@code }}
 */

        """.trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "{@code }"),
                Tok(INLINE_TAG_END, "}"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }

    test("Test empty inline tag") {


        """
/**
 * {@inheritDoc}
 */

        """.trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@inheritDoc"),
                Tok(INLINE_TAG_END),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )

    }

    test("Test block tag is interpreted in @code 2") {


        """
/**
 * {@code
 *
 * @param fullText
 * }
 */
            
        """.trim().shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                Tok(LINE_BREAK, "\n *"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(TAG_NAME, "@param"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "fullText"),
                Tok(LINE_BREAK, "\n *"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "}"),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }

    test("Test space before inline tag name doesn't push a tag_name") {

        // the <p> is interpreted as COMMENT_DATA inside the {@code}, but as HTML outside
        """/** some javadoc 
                <pre>{ @code { <p> } } <p> </pre> */
        """.shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "some javadoc"),
                Tok(WHITESPACE, " "),
                Tok(LINE_BREAK, "\n"),
                Tok(WHITESPACE, "                "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                // here's it's comment data
                Tok(COMMENT_DATA, "{ @code { "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "p"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " } } "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "p"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LCLOSE),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }

    test("Test bad character reference") {
        """/** & amp; */""".shouldHaveTokens(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "& amp;"),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_END)
        )
    }

})

data class Tok(val k: JdocTokenType, val im: String = k.constValue) {
    override fun toString(): String {
        return if (k.isConst) "Tok(${k.name})" else "Tok(${k.name}, \"${im.addEscapes()}\")"
    }

    private fun String.addEscapes() = replace("\n", "\\n").replace("\r", "\\r")
}

private fun JavadocLexer.consume(): List<JdocToken> = generateSequence { nextToken }.toList()

private fun JdocToken.assertMatches(ttype: JdocTokenType, start: Int, end: Int, image: String) {
    kind shouldBe ttype
    this.image shouldBe image
    assertEquals(start, startInDocument, "Wrong start offset")
    assertEquals(end, endInDocument, "Wrong end offset")
}


private fun String.shouldHaveTokens(vararg tokens: Tok) {
    val actual = JavadocLexer(this).consume().map { Tok(it.kind, it.image) }

    val diff = DiffUtils.diff(tokens.toList(), actual)
    if (diff.deltas.isEmpty()) {
        return
    }
    throw ComparisonFailure("Tokens didn't match", tokens.joinToString(separator = ",\n"), actual.joinToString(separator = ",\n"))
}
