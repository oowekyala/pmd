package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class MapExprTest : XPathParserTestSpec({

    parserTest("Test MapExpr") {

        "child::div1 / child::para / string() ! concat(\"id-\", .)" should matchExpr<ASTMapExpr> {
            //it::getOperands

            child<ASTPathExpr> {
                //it::getPathAnchor

                child<ASTAxisStep> {
                    it::isAbbrevAttributeAxis shouldBe false
                    it::isAbbrevDescendantOrSelf shouldBe false
                    it::isAbbrevNoAxis shouldBe false
                    it::isAbbrevParentNodeTest shouldBe false
                    //it::getAxis
                    //it::getPredicates

                    it::getNodeTest shouldBe child<ASTExactNameTest> {
                        it::getNameImage shouldBe "div1"

                        it::getNameNode shouldBe child {
                            it::getImage shouldBe "div1"
                            it::getLocalName shouldBe "div1"
                            it::getExplicitNamespacePrefix shouldBe null
                            it::isUriLiteral shouldBe false
                        }
                    }
                }
                child<ASTAxisStep> {
                    it::isAbbrevAttributeAxis shouldBe false
                    it::isAbbrevDescendantOrSelf shouldBe false
                    it::isAbbrevNoAxis shouldBe false
                    it::isAbbrevParentNodeTest shouldBe false
                    //it::getAxis
                    //it::getPredicates

                    it::getNodeTest shouldBe child<ASTExactNameTest> {
                        it::getNameImage shouldBe "para"

                        it::getNameNode shouldBe child {
                            it::getImage shouldBe "para"
                            it::getLocalName shouldBe "para"
                            it::getExplicitNamespacePrefix shouldBe null
                            it::isUriLiteral shouldBe false
                        }
                    }
                }
                child<ASTFunctionCall> {

                    it::getFunctionNameNode shouldBe child {
                        it::getImage shouldBe "string"
                        it::getLocalName shouldBe "string"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false
                    }
                    it::getArguments shouldBe child {
                    }
                }
            }
            child<ASTFunctionCall> {

                it::getFunctionNameNode shouldBe child {
                    it::getImage shouldBe "concat"
                    it::getLocalName shouldBe "concat"
                    it::getExplicitNamespacePrefix shouldBe null
                    it::isUriLiteral shouldBe false
                }
                it::getArguments shouldBe child {

                    child<ASTArgument> {
                        it::isPlaceholder shouldBe false

                        it::getExpression shouldBe child<ASTStringLiteral> {
                            //it::getDelimiter
                            it::getImage shouldBe "\"id-\""
                            it::getUnescapedValue shouldBe "id-"
                            it::getXmlUnescapedValue shouldBe "id-"
                        }
                    }
                    child<ASTArgument> {
                        it::isPlaceholder shouldBe false

                        it::getExpression shouldBe child<ASTContextItemExpr> {
                        }
                    }
                }
            }
        }


        "avg( //employee / salary ! translate(., '\$a', '') ! number(.))" should matchExpr<ASTFunctionCall> {

            it::getFunctionNameNode shouldBe child {
                it::getImage shouldBe "avg"
                it::getLocalName shouldBe "avg"
                it::getExplicitNamespacePrefix shouldBe null
                it::isUriLiteral shouldBe false
            }
            it::getArguments shouldBe child {

                child<ASTArgument> {
                    it::isPlaceholder shouldBe false

                    it::getExpression shouldBe child<ASTMapExpr> {
                        // it::getOperands shouldBe it::getChildren

                        child<ASTPathExpr> {
                            it::getPathAnchor shouldBe net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor.DESCENDANT_OR_ROOT

                            child<ASTAxisStep> {
                                it::isAbbrevAttributeAxis shouldBe false
                                it::isAbbrevDescendantOrSelf shouldBe false
                                it::isAbbrevNoAxis shouldBe true
                                it::isAbbrevParentNodeTest shouldBe false
                                it::getAxis shouldBe net.sourceforge.pmd.lang.xpath.ast.Axis.CHILD
                                //it::getPredicates

                                it::getNodeTest shouldBe child<ASTExactNameTest> {
                                    it::getNameImage shouldBe "employee"

                                    it::getNameNode shouldBe child {
                                        it::getImage shouldBe "employee"
                                        it::getLocalName shouldBe "employee"
                                        it::getExplicitNamespacePrefix shouldBe null
                                        it::isUriLiteral shouldBe false
                                    }
                                }
                            }
                            child<ASTAxisStep> {
                                it::isAbbrevAttributeAxis shouldBe false
                                it::isAbbrevDescendantOrSelf shouldBe false
                                it::isAbbrevNoAxis shouldBe true
                                it::isAbbrevParentNodeTest shouldBe false
                                it::getAxis shouldBe net.sourceforge.pmd.lang.xpath.ast.Axis.CHILD
                                //it::getPredicates

                                it::getNodeTest shouldBe child<ASTExactNameTest> {
                                    it::getNameImage shouldBe "salary"

                                    it::getNameNode shouldBe child {
                                        it::getImage shouldBe "salary"
                                        it::getLocalName shouldBe "salary"
                                        it::getExplicitNamespacePrefix shouldBe null
                                        it::isUriLiteral shouldBe false
                                    }
                                }
                            }
                        }
                        child<ASTFunctionCall> {

                            it::getFunctionNameNode shouldBe child {
                                it::getImage shouldBe "translate"
                                it::getLocalName shouldBe "translate"
                                it::getExplicitNamespacePrefix shouldBe null
                                it::isUriLiteral shouldBe false
                            }
                            it::getArguments shouldBe child {

                                child<ASTArgument> {
                                    it::isPlaceholder shouldBe false

                                    it::getExpression shouldBe child<ASTContextItemExpr> {
                                    }
                                }
                                child<ASTArgument> {
                                    it::isPlaceholder shouldBe false

                                    it::getExpression shouldBe child<ASTStringLiteral> {
                                        //it::getDelimiter
                                        it::getImage shouldBe "'\$a'"
                                        it::getUnescapedValue shouldBe "\$a"
                                        it::getXmlUnescapedValue shouldBe "\$a"
                                    }
                                }
                                child<ASTArgument> {
                                    it::isPlaceholder shouldBe false

                                    it::getExpression shouldBe child<ASTStringLiteral> {
                                        //it::getDelimiter
                                        it::getImage shouldBe "''"
                                        it::getUnescapedValue shouldBe ""
                                        it::getXmlUnescapedValue shouldBe ""
                                    }
                                }
                            }
                        }
                        child<ASTFunctionCall> {

                            it::getFunctionNameNode shouldBe child {
                                it::getImage shouldBe "number"
                                it::getLocalName shouldBe "number"
                                it::getExplicitNamespacePrefix shouldBe null
                                it::isUriLiteral shouldBe false
                            }
                            it::getArguments shouldBe child {

                                child<ASTArgument> {
                                    it::isPlaceholder shouldBe false

                                    it::getExpression shouldBe child<ASTContextItemExpr> {
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
