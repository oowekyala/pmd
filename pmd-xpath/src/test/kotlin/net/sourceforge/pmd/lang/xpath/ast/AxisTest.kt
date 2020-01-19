package net.sourceforge.pmd.lang.xpath.ast

import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 */
class AxisTest : XPathParserTestSpec({


    parserTest("Attribute may start a KindTest") {
        "/attribute(*)" should matchExpr<ASTPathExpr> {
            it::getPathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

            child<ASTAxisStep> {
                // This is an exception
                // The default axis is attribute when there's an attribute test
                it::getAxis shouldBe Axis.ATTRIBUTE

                child<ASTAttributeTest> {
                    it::getAttributeName shouldBe null
                    it::getTypeName shouldBe null
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
                it::getPathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

                child<ASTAxisStep> {
                    it::getAxis shouldBe axis

                    child<ASTExactNameTest> {
                        it::getNameNode shouldBe child {
                            it::getLocalName shouldBe "Node"
                        }
                    }
                }
            }
        }


        parserTest("Test explicit ${axis.axisName} axis use with conflicting name") {
            // The use after the :: is supposed to be interpreted as an element name
            "/${axis.axisName}::${axis.axisName}" should matchExpr<ASTPathExpr> {
                it::getPathAnchor shouldBe ASTPathExpr.PathAnchor.ROOT

                child<ASTAxisStep> {
                    it::getAxis shouldBe axis

                    child<ASTExactNameTest> {
                        it::getNameNode shouldBe child {
                            it::getLocalName shouldBe axis.axisName
                        }
                    }
                }
            }
        }
    }
})
