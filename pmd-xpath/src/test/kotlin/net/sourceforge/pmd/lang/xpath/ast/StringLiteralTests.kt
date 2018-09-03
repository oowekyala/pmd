package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.ast.TokenMgrError

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class StringLiteralTests : FunSpec({


    parserTest("Test double quoted string literal") {

        val string = "\"12.5\""

        string should matchExpr<ASTStringLiteral> {
            it.image shouldBe string
            it.unescapedValue shouldBe "12.5"
            it.delimiter shouldBe '"'
        }
    }

    parserTest("Test single quoted string literal") {

        val string = "'12.5'"

        string should matchExpr<ASTStringLiteral> {
            it.image shouldBe string
            it.unescapedValue shouldBe "12.5"
            it.delimiter shouldBe '\''
        }
    }

    parserTest("Test string delimiter escape \"\"") {

        val string = """
            "single single'double single''single double(not!)double double""hehe"
        """.trim()

        string should matchExpr<ASTStringLiteral> {
            it.image shouldBe string
            it.unescapedValue shouldBe """
                single single'double single''single double(not!)double double"hehe
            """.trim()
            it.delimiter shouldBe '"'
        }
    }



    parserTest("Test string delimiter escape ''") {

        val string = """
            'single single(not!)double single''single double"double double""hehe'
        """.trim()

        string should matchExpr<ASTStringLiteral> {
            it.image shouldBe string
            it.unescapedValue shouldBe """
                single single(not!)double single'single double"double double""hehe
            """.trim()
            it.delimiter shouldBe '\''
        }
    }

    parserTest("Test xml unescaped string literal") {

        val string = "'List&lt;Int&gt;'"

        string should matchExpr<ASTStringLiteral> {
            it.image shouldBe string
            it.unescapedValue shouldBe "List&lt;Int&gt;"
            it.xmlUnescapedValue shouldBe "List<Int>"
            it.delimiter shouldBe '\''
        }
    }

    parserTest("Test comment in strings") {
        "'this is just a string :)'" should matchExpr<ASTStringLiteral> {
            it.unescapedValue shouldBe "this is just a string :)"
        }

        "'this is another string (:'" should matchExpr<ASTStringLiteral> {
            it.unescapedValue shouldBe "this is another string (:"
        }
    }

    parserTest("Test unbalanced comment markers in string") {
        expect<TokenMgrError>() whenParsing {
            "(: \"this is just a string :)\" :)"
        }

        expect<ParseException>() whenParsing {
            "(: \"this is another string (:\" :)"
        }
    }
})