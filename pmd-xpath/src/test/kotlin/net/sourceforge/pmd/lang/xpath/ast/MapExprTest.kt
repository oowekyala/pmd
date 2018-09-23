package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class MapExprTest : FunSpec({


    testGroup("Test MapExpr") {

        "child::div1 / child::para / string() ! concat(\"id-\", .)" should matchExpr<ASTMapExpr> {
            //it.operands

            child<ASTPathExpr> {
                //it.pathAnchor

                child<ASTAxisStep> {
                    it.isAbbrevAttributeAxis shouldBe false
                    it.isAbbrevDescendantOrSelf shouldBe false
                    it.isAbbrevNoAxis shouldBe false
                    it.isAbbrevParentNodeTest shouldBe false
                    //it.axis
                    //it.predicates

                    it.nodeTest shouldBe child<ASTExactNameTest> {
                        it.nameImage shouldBe "div1"

                        it.nameNode shouldBe child {
                            it.image shouldBe "div1"
                            it.localName shouldBe "div1"
                            it.namespacePrefix shouldBe ""
                            it.isUriLiteral shouldBe false
                        }
                    }
                }
                child<ASTAxisStep> {
                    it.isAbbrevAttributeAxis shouldBe false
                    it.isAbbrevDescendantOrSelf shouldBe false
                    it.isAbbrevNoAxis shouldBe false
                    it.isAbbrevParentNodeTest shouldBe false
                    //it.axis
                    //it.predicates

                    it.nodeTest shouldBe child<ASTExactNameTest> {
                        it.nameImage shouldBe "para"

                        it.nameNode shouldBe child {
                            it.image shouldBe "para"
                            it.localName shouldBe "para"
                            it.namespacePrefix shouldBe ""
                            it.isUriLiteral shouldBe false
                        }
                    }
                }
                child<ASTFunctionCall> {

                    it.functionName shouldBe child {
                        it.image shouldBe "string"
                        it.localName shouldBe "string"
                        it.namespacePrefix shouldBe ""
                        it.isUriLiteral shouldBe false
                    }
                    it.arguments shouldBe child {
                    }
                }
            }
            child<ASTFunctionCall> {

                it.functionName shouldBe child {
                    it.image shouldBe "concat"
                    it.localName shouldBe "concat"
                    it.namespacePrefix shouldBe ""
                    it.isUriLiteral shouldBe false
                }
                it.arguments shouldBe child {

                    child<ASTArgument> {
                        it.isPlaceholder shouldBe false

                        it.expression shouldBe child<ASTStringLiteral> {
                            //it.delimiter
                            it.image shouldBe "\"id-\""
                            it.unescapedValue shouldBe "id-"
                            it.xmlUnescapedValue shouldBe "id-"
                        }
                    }
                    child<ASTArgument> {
                        it.isPlaceholder shouldBe false

                        it.expression shouldBe child<ASTContextItemExpr> {
                        }
                    }
                }
            }
        }


        "avg( //employee / salary ! translate(., '\$a', '') ! number(.))" should matchExpr<ASTFunctionCall> {

            it.functionName shouldBe child {
                it.image shouldBe "avg"
                it.localName shouldBe "avg"
                it.namespacePrefix shouldBe ""
                it.isUriLiteral shouldBe false
            }
            it.arguments shouldBe child {

                child<ASTArgument> {
                    it.isPlaceholder shouldBe false

                    it.expression shouldBe child<ASTMapExpr> {
                        // it.operands shouldBe it.children

                        child<ASTPathExpr> {
                            it.pathAnchor shouldBe net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.DESCENDANT_OR_ROOT

                            child<ASTAxisStep> {
                                it.isAbbrevAttributeAxis shouldBe false
                                it.isAbbrevDescendantOrSelf shouldBe false
                                it.isAbbrevNoAxis shouldBe true
                                it.isAbbrevParentNodeTest shouldBe false
                                it.axis shouldBe net.sourceforge.pmd.lang.xpath.ast.Axis.CHILD
                                //it.predicates

                                it.nodeTest shouldBe child<ASTExactNameTest> {
                                    it.nameImage shouldBe "employee"

                                    it.nameNode shouldBe child {
                                        it.image shouldBe "employee"
                                        it.localName shouldBe "employee"
                                        it.namespacePrefix shouldBe ""
                                        it.isUriLiteral shouldBe false
                                    }
                                }
                            }
                            child<ASTAxisStep> {
                                it.isAbbrevAttributeAxis shouldBe false
                                it.isAbbrevDescendantOrSelf shouldBe false
                                it.isAbbrevNoAxis shouldBe true
                                it.isAbbrevParentNodeTest shouldBe false
                                it.axis shouldBe net.sourceforge.pmd.lang.xpath.ast.Axis.CHILD
                                //it.predicates

                                it.nodeTest shouldBe child<ASTExactNameTest> {
                                    it.nameImage shouldBe "salary"

                                    it.nameNode shouldBe child {
                                        it.image shouldBe "salary"
                                        it.localName shouldBe "salary"
                                        it.namespacePrefix shouldBe ""
                                        it.isUriLiteral shouldBe false
                                    }
                                }
                            }
                        }
                        child<ASTFunctionCall> {

                            it.functionName shouldBe child {
                                it.image shouldBe "translate"
                                it.localName shouldBe "translate"
                                it.namespacePrefix shouldBe ""
                                it.isUriLiteral shouldBe false
                            }
                            it.arguments shouldBe child {

                                child<ASTArgument> {
                                    it.isPlaceholder shouldBe false

                                    it.expression shouldBe child<ASTContextItemExpr> {
                                    }
                                }
                                child<ASTArgument> {
                                    it.isPlaceholder shouldBe false

                                    it.expression shouldBe child<ASTStringLiteral> {
                                        //it.delimiter
                                        it.image shouldBe "'${'$'}a'"
                                        it.unescapedValue shouldBe "${'$'}a"
                                        it.xmlUnescapedValue shouldBe "${'$'}a"
                                    }
                                }
                                child<ASTArgument> {
                                    it.isPlaceholder shouldBe false

                                    it.expression shouldBe child<ASTStringLiteral> {
                                        //it.delimiter
                                        it.image shouldBe "''"
                                        it.unescapedValue shouldBe ""
                                        it.xmlUnescapedValue shouldBe ""
                                    }
                                }
                            }
                        }
                        child<ASTFunctionCall> {

                            it.functionName shouldBe child {
                                it.image shouldBe "number"
                                it.localName shouldBe "number"
                                it.namespacePrefix shouldBe ""
                                it.isUriLiteral shouldBe false
                            }
                            it.arguments shouldBe child {

                                child<ASTArgument> {
                                    it.isPlaceholder shouldBe false

                                    it.expression shouldBe child<ASTContextItemExpr> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
})