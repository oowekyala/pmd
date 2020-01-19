/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Node kinds defined by the XQuery and XPath data model.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public enum InfixOperator {


    OR("or", XPathTokenKinds.OR),
    AND("and", XPathTokenKinds.AND),

    DIV("div", XPathTokenKinds.DIV),
    IDIV("idiv", XPathTokenKinds.IDIV),
    MOD("mod", XPathTokenKinds.MOD),
    UNION("union", XPathTokenKinds.UNION),
    INTERSECT("intersect", XPathTokenKinds.INTERSECT),
    EXCEPT("except", XPathTokenKinds.EXCEPT),


    // general comparison
    EQ("=", XPathTokenKinds.SEP_EQ),
    NE("!=", XPathTokenKinds.SEP_NE),
    LT("<", XPathTokenKinds.SEP_LT),
    LE("<=", XPathTokenKinds.SEP_LE),
    GT(">", XPathTokenKinds.SEP_GT),
    GE(">=", XPathTokenKinds.SEP_GE),

    // node comparison
    NODE_FOLLOWS(">>", XPathTokenKinds.SEP_NODE_FOLLOWS),
    NODE_PRECEDES("<<", XPathTokenKinds.SEP_NODE_PRECEDES),
    NODE_IS("is", XPathTokenKinds.IS),

    // value comparisons
    VAL_NE("ne", XPathTokenKinds.NE),
    VAL_EQ("eq", XPathTokenKinds.EQ),
    VAL_LT("lt", XPathTokenKinds.LT),
    VAL_LE("le", XPathTokenKinds.LE),
    VAL_GT("gt", XPathTokenKinds.GT),
    VAL_GE("ge", XPathTokenKinds.GE),

    STRING_CONCAT("||", XPathTokenKinds.DOUBLE_PIPE),


    ;

    InfixOperator(String image, int tokenKind) {

    }
}
