package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.matchers.collections.shouldContainAll
import net.sourceforge.pmd.lang.ast.test.shouldBe
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class InlineFunctionExprTest : XPathParserTestSpec({

    parserTest("Test inline function expr") {

        "function() as xs:integer+ { 2, 3, 5, 7, 11, 13 }" should matchExpr<ASTInlineFunctionExpr> {
            it::isDefaultReturnType shouldBe false

            it::getParamList shouldBe child {

            }
            it::getDeclaredReturnType shouldBe child {
                it::getCardinality shouldBe Cardinality.ONE_OR_MORE
                it::isEmptySequence shouldBe false

                it::getItemType shouldBe child<ASTAtomicOrUnionType> {

                    it::getTypeNameNode shouldBe child {
                        it::getImage shouldBe "xs:integer"
                        it::getLocalName shouldBe "integer"
                        it::getExplicitNamespacePrefix shouldBe "xs"
                        it::isUriLiteral shouldBe false

                    }
                }
            }
            it::getBodyExpr shouldBe child<ASTSequenceExpr>(ignoreChildren = true) {}
        }


        "function(${'$'}a as xs:double, ${'$'}b as xs:double) as xs:double { ${'$'}a * ${'$'}b }" should matchExpr<ASTInlineFunctionExpr> {
            it::isDefaultReturnType shouldBe false

            it::getParamList shouldBe child {

                child<ASTParam> {
                    it::isDefaultType shouldBe false

                    it::getNameNode shouldBe child {
                        it::getImage shouldBe "a"
                        it::getLocalName shouldBe "a"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false

                    }
                    it::getDeclaredType shouldBe child<ASTSequenceType> {
                        it::getCardinality shouldBe Cardinality.EXACTLY_ONE
                        it::isEmptySequence shouldBe false

                        it::getItemType shouldBe child<ASTAtomicOrUnionType> {

                            it::getTypeNameNode shouldBe child {
                                it::getImage shouldBe "xs:double"
                                it::getLocalName shouldBe "double"
                                it::getExplicitNamespacePrefix shouldBe "xs"
                                it::isUriLiteral shouldBe false

                            }
                        }
                    }
                }
                child<ASTParam> {
                    it::isDefaultType shouldBe false

                    it::getNameNode shouldBe child {
                        it::getImage shouldBe "b"
                        it::getLocalName shouldBe "b"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false

                    }
                    it::getDeclaredType shouldBe child<ASTSequenceType> {
                        it::getCardinality shouldBe Cardinality.EXACTLY_ONE
                        it::isEmptySequence shouldBe false

                        it::getItemType shouldBe child<ASTAtomicOrUnionType> {

                            it::getTypeNameNode shouldBe child {
                                it::getImage shouldBe "xs:double"
                                it::getLocalName shouldBe "double"
                                it::getExplicitNamespacePrefix shouldBe "xs"
                                it::isUriLiteral shouldBe false

                            }
                        }
                    }
                }
            }
            it::getDeclaredReturnType shouldBe child<ASTSequenceType> {
                it::getCardinality shouldBe Cardinality.EXACTLY_ONE
                it::isEmptySequence shouldBe false

                it::getItemType shouldBe child<ASTAtomicOrUnionType> {

                    it::getTypeNameNode shouldBe child {
                        it::getImage shouldBe "xs:double"
                        it::getLocalName shouldBe "double"
                        it::getExplicitNamespacePrefix shouldBe "xs"
                        it::isUriLiteral shouldBe false

                    }
                }
            }
            it::getBodyExpr shouldBe child<ASTMultiplicativeExpr> {

                child<ASTVarRef> {

                    it::getVarNameNode shouldBe child {
                        it::getImage shouldBe "a"
                        it::getLocalName shouldBe "a"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false

                    }
                }
                child<ASTMultiplicativeOperator> {
                    it::getImage shouldBe "*"
                }
                child<ASTVarRef> {

                    it::getVarNameNode shouldBe child {
                        it::getImage shouldBe "b"
                        it::getLocalName shouldBe "b"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false

                    }
                }
            }
        }

        "function(${'$'}a) { ${'$'}a }" should matchExpr<ASTInlineFunctionExpr> {
            it::getDeclaredReturnType shouldBe null
            it::isDefaultReturnType shouldBe true

            it::getParamList shouldBe child {

                child<ASTParam> {
                    it::getDeclaredType shouldBe null
                    it::isDefaultType shouldBe true

                    it::getNameNode shouldBe child {
                        it::getImage shouldBe "a"
                        it::getLocalName shouldBe "a"
                        it::getExplicitNamespacePrefix shouldBe null
                        it::isUriLiteral shouldBe false

                    }
                }
            }
            it::getBodyExpr shouldBe child<ASTVarRef> {

                it::getVarNameNode shouldBe child {
                    it::getImage shouldBe "a"
                    it::getLocalName shouldBe "a"
                    it::getExplicitNamespacePrefix shouldBe null
                    it::isUriLiteral shouldBe false

                }
            }
        }

        "collection()/(let ${'$'}a := . return function() { ${'$'}a })" should matchExpr<ASTPathExpr> {
            //it::getPathAnchor

            child<ASTFunctionCall> {

                it::getFunctionNameNode shouldBe child {
                    it::getImage shouldBe "collection"
                    it::getLocalName shouldBe "collection"
                    it::getExplicitNamespacePrefix shouldBe null
                    it::isUriLiteral shouldBe false

                }
                it::getArguments shouldBe child {}
            }
            child<ASTParenthesizedExpr> {

                it::getWrappedNode shouldBe child<ASTLetExpr> {

                    val binding = child<ASTVarBinding> {
                        it::isLetStyle shouldBe true
                        it::getVarName shouldBe "a"

                        it::getVarNameNode shouldBe child {
                            it::getImage shouldBe "a"
                            it::getLocalName shouldBe "a"
                            it::getExplicitNamespacePrefix shouldBe null
                            it::isUriLiteral shouldBe false

                        }
                        it::getInitializerExpr shouldBe child<ASTContextItemExpr> {

                        }
                    }

                    it.bindings.toList().shouldContainAll(binding)


                    it::getBodyExpr shouldBe child<ASTInlineFunctionExpr> {
                        it::getDeclaredReturnType shouldBe null
                        it::isDefaultReturnType shouldBe true

                        it::getParamList shouldBe child {}

                        it::getBodyExpr shouldBe child<ASTVarRef> {

                            it::getVarNameNode shouldBe child {
                                it::getImage shouldBe "a"
                                it::getLocalName shouldBe "a"
                                it::getExplicitNamespacePrefix shouldBe null
                                it::isUriLiteral shouldBe false

                            }
                        }
                    }
                }
            }
        }


    }
})