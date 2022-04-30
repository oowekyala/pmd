/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.sourceforge.pmd.lang.ast.test.IntelliMarker
import net.sourceforge.pmd.lang.document.TextRegion
import net.sourceforge.pmd.lang.java.ast.makeJavaTranslatedDocument
import net.sourceforge.pmd.lang.javadoc.ast.JdocTokenType.*
import org.assertj.core.util.diff.DiffUtils
import org.junit.ComparisonFailure
import kotlin.test.assertEquals


private fun newLexer(code: String, start: Int = 0, end: Int = code.length): JavadocLexer {
    return JavadocLexer(makeJavaTranslatedDocument(code.substring(start, end)))
}

class JavadocLexerTest : IntelliMarker, FunSpec({


    test("Test trailing chars are ignored") {

        val lexer = newLexer("/** some javadoc */  ...")

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = 3, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 3, end = 4, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 4, end = 16, image = "some javadoc")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 16, end = 17, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 17, end = 19, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at EOF") {

        val lexer = newLexer("/** some javadoc ")

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = 3, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 3, end = 4, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 4, end = 17, image = "some javadoc ")
        lexer.nextToken shouldBe null
    }

    test("Test java unicode escapes") {

        val comment = """\u002F\u002a\u002a\u002a\u002F"""

        val lexer = newLexer(comment)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = 3, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 3, end = 5, image = "*/")
        lexer.nextToken shouldBe null

    }


    test("Test java escaped unicode escapes") {

        val comment = """\u002F\u002a\u002a\\u002a\u002F"""

        val lexer = newLexer(comment)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 0, end = 3, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 3, end = 11, image = "\\\\u002a/")
        lexer.nextToken shouldBe null

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
            Tok(PARAM_NAME, "fileText"),
            Tok(WHITESPACE, "    "),
            Tok(COMMENT_DATA, "Full file text"),
            Tok(LINE_BREAK, "\n *"),
            Tok(WHITESPACE, " "),
            Tok(TAG_NAME, "@param"),
            Tok(WHITESPACE, " "),
            Tok(PARAM_NAME, "startOffset"),
            Tok(WHITESPACE, " "),
            Tok(COMMENT_DATA, "Start offset in the file text"),
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
            Tok(PARAM_NAME, "fileText"),
            Tok(LINE_BREAK, "\n *"),
            Tok(WHITESPACE, "          "),
            Tok(COMMENT_DATA, "Full file text"),
            Tok(LINE_BREAK, "\n *"),
            Tok(WHITESPACE, " "),
            Tok(TAG_NAME, "@param"),
            Tok(WHITESPACE, " "),
            Tok(PARAM_NAME, "startOffset"),
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
            Tok(PARAM_NAME, "fullText"),
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
            Tok(PARAM_NAME, "fullText"),
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
    assertEquals(TextRegion.fromBothOffsets(start, end), region)
}


private fun String.shouldHaveTokens(vararg tokens: Tok) {
    val actual = newLexer(this).consume().map { Tok(it.kind, it.image) }

    val diff = DiffUtils.diff(tokens.toList(), actual)
    if (diff.deltas.isEmpty()) {
        return
    }
    throw ComparisonFailure(
        "Tokens didn't match",
        tokens.joinToString(separator = ",\n"),
        actual.joinToString(separator = ",\n")
    )
}
