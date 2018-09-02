package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.ROOT

/**
 * https://www.w3.org/TR/xpath-30/#doc-xpath30-RelativePathExpr
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
class LoneSingleSlashConstraintTest : FunSpec({

    /*

    Excerpt of https://www.w3.org/TR/xpath-30/#parse-note-leading-lone-slash

    A single slash may appear either as a complete path expression or
    as the first part of a path expression in which it is followed by
    a RelativePathExpr. In some cases, the next token after the slash
    is insufficient to allow a parser to distinguish these two possibilities:
    the * token and keywords like union could be either an operator or
    a NameTest. For example, without lookahead the first part of the
    expression / * 5 is easily taken to be a complete expression, / *,
    which has a very different interpretation (the child nodes of /).

    Therefore to reduce the need for lookahead, if the token immediately
    following a slash can form the start of a RelativePathExpr, then the
    slash must be the beginning of a PathExpr, not the entirety of it.

    A single slash may be used as the left-hand argument of an operator
    by parenthesizing it: (/) * 5. The expression 5 * /, on the other hand,
    is syntactically valid without parentheses.

    */

    parserTest("Test ambiguity with single slash path: /*5") {
        parserShouldFailOn {
            "/*5"
        }
    }


    parserTest("Test ambiguity with single slash path: 5*/") {
        "5*/" should matchExpr<ASTMultiplicativeExpr> {

            child<ASTNumericLiteral> {
                it.image shouldBe "5"
            }

            child<ASTTimesOperator> { }

            child<ASTPathExpr> {
                it.pathAnchor shouldBe ROOT
            }
        }
    }

    parserTest("Test ambiguity with single slash path: (/)*5") {
        "(/)*5" should matchExpr<ASTMultiplicativeExpr> {
            child<ASTParenthesizedExpr> {
                child<ASTExpr> {
                    child<ASTPathExpr> {
                        it.pathAnchor shouldBe ROOT
                    }
                }
            }

            child<ASTTimesOperator> { }

            child<ASTNumericLiteral> {
                it.image shouldBe "5"
            }
        }
    }

    parserTest("Test ambiguity with single slash path: / union /") {
        "/ union /*" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTStepExpr> {
                child<ASTAxisStep> {
                    child<ASTExactNameTest> {
                        it.nameImage shouldBe "union"
                    }

                    child<ASTPredicateList> { }
                }
            }

            child<ASTStepExpr> {
                child<ASTAxisStep> {
                    child<ASTWildcardNameTest> {
                    }

                    child<ASTPredicateList> { }
                }
            }
        }
    }

    parserTest("Test ambiguity with wildcard: /*") {
        "/*" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ROOT

            child<ASTStepExpr> {
                child<ASTAxisStep> {
                    child<ASTWildcardNameTest> {
                    }

                    child<ASTPredicateList> { }
                }
            }
        }
    }

})