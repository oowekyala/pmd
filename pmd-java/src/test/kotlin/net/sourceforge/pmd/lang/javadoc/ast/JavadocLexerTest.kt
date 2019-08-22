/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.javadoc.ast.JavadocTokenType.*


class JavadocLexerTest : FunSpec({


    test("Test out of bounds max offset gives EOF") {

        val code = "01234567/** some javadoc */"
        //                  ^

        val lexer = JavadocLexerAdapter(code, 8, code.length + 10)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 25, image = "some javadoc ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 25, end = 27, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at COMMENT_END") {

        val comment = "/** some javadoc */"
        val code = "01234567${comment}public void foo()"
        //                  ^
        val lexer = JavadocLexerAdapter(code, 8, 8 + comment.length + 3)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 25, image = "some javadoc ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_END, start = 25, end = 27, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at EOF") {

        val code = "01234567/** some javadoc "
        //                  ^
        val lexer = JavadocLexerAdapter(code, 8, 100)

        lexer.nextToken!!.assertMatches(ttype = COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = COMMENT_DATA, start = 12, end = 25, image = "some javadoc ")
        lexer.nextToken shouldBe null
    }

    test("Test brace balancing") {

        // the <p> is interpreted as COMMENT_DATA inside the {@code}, but as HTML outside
        val code = """/** some javadoc 
                <pre>{@code { <p> } } <p> </pre> */
            """

        // ["/**", whitespace, comment data, whitespace, comment data, "{", tag name, comment data, "{", comment data, "}", comment data, "}", comment data, "*/"]
        val tokens = JavadocLexerAdapter(code, 0, 100).consume().map { Tok(it.kind, it.image) }

        tokens shouldBe listOf(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "some javadoc "),
                Tok(LINE_BREAK, "\n                "),
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
                Tok(COMMENT_DATA, " "),
                Tok(COMMENT_END)
        )
    }
    test("Test line breaks") {

        val code = """
/**
 * @param fileText    Full file text
 * @param startOffset Start offset in the file text
 */
            
        """.trimIndent()

        val tokens = JavadocLexerAdapter(code).consume().map { Tok(it.kind, it.image) }

        tokens shouldBe listOf(
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
                Tok(LINE_BREAK, "\n "),
                Tok(COMMENT_END)
        )

    }

    test("Test param") {

        val code = """
/**
 * @param fileText
 *          Full file text
 * @param startOffset
 *          Start offset in the file text
 */
            
        """.trimIndent()

        val tokens = JavadocLexerAdapter(code).consume().map { Tok(it.kind, it.image) }

        tokens shouldBe listOf(
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
                Tok(LINE_BREAK, "\n "),
                Tok(COMMENT_END)
        )
    }

    test("Test space before inline tag name doesn't push a tag_name") {

        // the <p> is interpreted as COMMENT_DATA inside the {@code}, but as HTML outside
        val code = """/** some javadoc 
                <pre>{ @code { <p> } } <p> </pre> */
            """

        // ["/**", whitespace, comment data, whitespace, comment data, "{", tag name, comment data, "{", comment data, "}", comment data, "}", comment data, "*/"]
        val tokens = JavadocLexerAdapter(code, 0, 100).consume().map { Tok(it.kind, it.image) }

        tokens shouldBe listOf(
                Tok(COMMENT_START),
                Tok(WHITESPACE, " "),
                Tok(COMMENT_DATA, "some javadoc "),
                Tok(LINE_BREAK, "\n                "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(INLINE_TAG_START),
                // here's it's comment data
                Tok(COMMENT_DATA, " @code "),
                Tok(INLINE_TAG_START),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "p"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " "),
                Tok(INLINE_TAG_END),
                Tok(COMMENT_DATA, " "),
                Tok(INLINE_TAG_END),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "p"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " "),
                Tok(HTML_LCLOSE),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(COMMENT_DATA, " "),
                Tok(COMMENT_END)
        )
    }


})

data class Tok(val k: JavadocTokenType, val im: String = k.constValue) {
    override fun toString(): String {
        return if (k.isConst) "Tok(${k.name})" else "Tok(${k.name}, \"${im.addEscapes()}\")"
    }

    private fun String.addEscapes() = replace("\n", "\\n").replace("\r", "\\r")
}

private fun JavadocLexerAdapter.consume(): List<JavadocToken> = generateSequence { nextToken }.toList()

private fun JavadocToken.assertMatches(ttype: JavadocTokenType, start: Int, end: Int, image: String) {
    kind shouldBe ttype
    this.image shouldBe image
    startInDocument shouldBe start
    endInDocument shouldBe end
}
