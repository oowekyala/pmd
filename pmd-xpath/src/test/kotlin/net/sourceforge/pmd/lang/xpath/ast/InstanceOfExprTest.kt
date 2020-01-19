package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class InstanceOfExprTest : XPathParserTestSpec({

    parserTest("Test instance of expr") {


        "5 instance of xs:integer" should matchExpr<ASTInstanceofExpr> {
            child<ASTNumericLiteral> {
                it::getIntValue shouldBe 5
            }

            child<ASTSequenceType> {
                it::getItemType shouldBe child<ASTAtomicOrUnionType> {
                    it::getTypeNameNode shouldBe child {
                        it::getExplicitNamespacePrefix shouldBe "xs"
                        it::getLocalName shouldBe "integer"
                    }
                }
                it::getCardinality shouldBe Cardinality.EXACTLY_ONE
            }
        }

        "(5, 6) instance of xs:integer+" should matchExpr<ASTInstanceofExpr> {
            child<ASTSequenceExpr>(ignoreChildren = true) {
                it::getParenDepth shouldBe 1
            }

            child<ASTSequenceType> {
                it::getItemType shouldBe child<ASTAtomicOrUnionType> {
                    it::getTypeNameNode shouldBe child {
                        it::getExplicitNamespacePrefix shouldBe "xs"
                        it::getLocalName shouldBe "integer"
                    }
                }
                it::getCardinality shouldBe Cardinality.ONE_OR_MORE
            }
        }

        ". instance of element()" should matchExpr<ASTInstanceofExpr> {
            child<ASTContextItemExpr> {}

            child<ASTSequenceType> {
                it::getItemType shouldBe child<ASTElementTest> {
                    it::isEmptyParen shouldBe true
                    it::isOptionalType shouldBe false
                    it::getTypeName shouldBe null
                    it::getElementName shouldBe null
                }
                it::getCardinality shouldBe Cardinality.EXACTLY_ONE
            }
        }


    }


})
