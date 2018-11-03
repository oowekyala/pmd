package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.util.*

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class InstanceOfExprTest : FunSpec({

    testGroup("Test instance of expr") {


        "5 instance of xs:integer" should matchExpr<ASTInstanceofExpr> {
            child<ASTNumericLiteral> {
                it.intValue shouldBe 5
            }

            child<ASTSequenceType> {
                it.itemType shouldBe child<ASTAtomicOrUnionType> {
                    it.typeNameNode shouldBe child {
                        it.explicitNamespacePrefix shouldBe Optional.of("xs")
                        it.localName shouldBe "integer"
                    }
                }
                it.cardinality shouldBe Cardinality.EXACTLY_ONE
            }
        }

        "(5, 6) instance of xs:integer+" should matchExpr<ASTInstanceofExpr> {
            child<ASTParenthesizedExpr> {
                child<ASTSequenceExpr>(ignoreChildren = true) {}
            }

            child<ASTSequenceType> {
                it.itemType shouldBe child<ASTAtomicOrUnionType> {
                    it.typeNameNode shouldBe child {
                        it.explicitNamespacePrefix shouldBe Optional.of("xs")
                        it.localName shouldBe "integer"
                    }
                }
                it.cardinality shouldBe Cardinality.ONE_OR_MORE
            }
        }

        ". instance of element()" should matchExpr<ASTInstanceofExpr> {
            child<ASTContextItemExpr> {}

            child<ASTSequenceType> {
                it.itemType shouldBe child<ASTElementTest> {
                    it.isEmptyParen shouldBe true
                    it.isOptionalType shouldBe false
                    it.typeName shouldBe Optional.empty()
                    it.elementName shouldBe Optional.empty()
                }
                it.cardinality shouldBe Cardinality.EXACTLY_ONE
            }
        }


    }


})