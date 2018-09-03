package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class AxisTest : FunSpec({


    parserTest("Attribute may start a KindTest") {
        "/attribute(*)" should matchExpr<ASTPathExpr> {
            it.pathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

            child<ASTAxisStep> {
                // This is an exception
                // The default axis is attribute when there's an attribute test
                it.axis shouldBe Axis.ATTRIBUTE

                child<ASTAttributeTest> {
                    child<ASTAttributeNameOrWildCard> { }
                }
            }
        }
    }

    parserTest("Unrecognised axis should fail parsing") {
        expect<ParseException>() whenParsing {
            "//hey/wrong::foo"
        }
    }



    // generate some tests for each axis
    Axis.values().forEach { axis ->

        parserTest("Test explicit ${axis.axisName} axis use") {
            "/${axis.axisName}::Node" should matchExpr<ASTPathExpr> {
                it.pathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

                child<ASTAxisStep> {
                    it.axis shouldBe axis

                    child<ASTExactNameTest> {
                        it.nameNode shouldBe child {
                            it.localName shouldBe "Node"
                        }
                    }
                }
            }
        }


        parserTest("Test explicit ${axis.axisName} axis use with conflicting name") {
            // The use after the :: is supposed to be interpreted as an element name
            "/${axis.axisName}::${axis.axisName}" should matchExpr<ASTPathExpr> {
                it.pathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

                child<ASTAxisStep> {
                    it.axis shouldBe axis

                    child<ASTExactNameTest> {
                        it.nameNode shouldBe child {
                            it.localName shouldBe axis.axisName
                        }
                    }
                }
            }
        }
    }
})