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
                Tok(WHITESPACE, "\n                "),
                Tok(HTML_LT),
                Tok(HTML_IDENT, "pre"),
                Tok(HTML_GT),
                Tok(INLINE_TAG_START),
                Tok(TAG_NAME, "@code"),
                // here's it's comment data
                Tok(COMMENT_DATA, " { <p> } "),
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

data class Tok(val k: JavadocTokenType, val im: String = k.constValue)

private fun JavadocLexerAdapter.consume(): List<JavadocToken> = generateSequence { nextToken }.toList()

private fun JavadocToken.assertMatches(ttype: JavadocTokenType, start: Int, end: Int, image: String) {
    kind shouldBe ttype
    this.image shouldBe image
    startInDocument shouldBe start
    endInDocument shouldBe end
}
