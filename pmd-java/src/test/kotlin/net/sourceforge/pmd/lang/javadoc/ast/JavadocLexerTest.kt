/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec


class JavadocLexerTest : FunSpec({


    test("Test out of bounds max offset gives EOF") {

        val code = "01234567/** some javadoc */"
        //                  ^

        val lexer = JavadocLexerAdapter(code, 8, code.length + 10)

        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_DATA, start = 12, end = 24, image = "some javadoc")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_END, start = 24, end = 26, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at COMMENT_END") {

        val comment = "/** some javadoc */"
        val code = "01234567${comment}public void foo()"
        //                  ^
        val lexer = JavadocLexerAdapter(code, 8, 8 + comment.length + 3)

        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_DATA, start = 12, end = 24, image = "some javadoc")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_END, start = 24, end = 26, image = "*/")
        lexer.nextToken shouldBe null
    }

    test("Test lexing stops anyway at EOF") {

        val code = "01234567/** some javadoc "
        //                  ^
        val lexer = JavadocLexerAdapter(code, 8, 100)

        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_START, start = 8, end = 11, image = "/**")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.WHITESPACE, start = 11, end = 12, image = " ")
        lexer.nextToken!!.assertMatches(ttype = JavadocTokenType.COMMENT_DATA, start = 12, end = 24, image = "some javadoc")
        lexer.nextToken shouldBe null
    }


})

private fun JavadocToken.assertMatches(ttype: JavadocTokenType, start: Int, end: Int, image: String) {
    kind shouldBe ttype
    this.image shouldBe image
    startInDocument shouldBe start
    endInDocument shouldBe end
}
