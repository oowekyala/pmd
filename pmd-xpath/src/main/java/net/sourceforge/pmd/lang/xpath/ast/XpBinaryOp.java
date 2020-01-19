/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Binary operator
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public enum XpBinaryOp {


    OR("or", XPathTokenKinds.OR),
    AND("and", XPathTokenKinds.AND),

    ADD("+", XPathTokenKinds.PLUS),
    SUB("-", XPathTokenKinds.MINUS),


    MUL("*", XPathTokenKinds.STAR),
    DIV("div", XPathTokenKinds.DIV),
    IDIV("idiv", XPathTokenKinds.IDIV),
    MOD("mod", XPathTokenKinds.MOD),


    UNION("union", XPathTokenKinds.UNION),
    UNION_SHORT("|", XPathTokenKinds.PIPE),

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

    RANGE("..", XPathTokenKinds.DOUBLE_PERIOD),
    STRING_CONCAT("||", XPathTokenKinds.DOUBLE_PIPE),
    ;

    private final String image;
    private final int tokenKind;

    XpBinaryOp(String image, int tokenKind) {

        this.image = image;
        this.tokenKind = tokenKind;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return image;
    }

    public boolean isUnion() {
        return this == UNION || this == UNION_SHORT;
    }

    static XpBinaryOp fromTokenKind(int kind) {
        switch (kind) {

        case XPathTokenKinds.OR:
            return OR;
        case XPathTokenKinds.AND:
            return AND;

        case XPathTokenKinds.PLUS:
            return ADD;
        case XPathTokenKinds.MINUS:
            return SUB;

        case XPathTokenKinds.STAR:
            return MUL;
        case XPathTokenKinds.DIV:
            return DIV;
        case XPathTokenKinds.IDIV:
            return IDIV;
        case XPathTokenKinds.MOD:
            return MOD;

        case XPathTokenKinds.UNION:
            return UNION;
        case XPathTokenKinds.PIPE:
            return UNION_SHORT;

        case XPathTokenKinds.INTERSECT:
            return INTERSECT;
        case XPathTokenKinds.EXCEPT:
            return EXCEPT;

        case XPathTokenKinds.SEP_EQ:
            return EQ;
        case XPathTokenKinds.SEP_NE:
            return NE;
        case XPathTokenKinds.SEP_LT:
            return LT;
        case XPathTokenKinds.SEP_LE:
            return LE;
        case XPathTokenKinds.SEP_GT:
            return GT;
        case XPathTokenKinds.SEP_GE:
            return GE;

        case XPathTokenKinds.SEP_NODE_FOLLOWS:
            return NODE_FOLLOWS;
        case XPathTokenKinds.SEP_NODE_PRECEDES:
            return NODE_PRECEDES;
        case XPathTokenKinds.IS:
            return NODE_IS;

        case XPathTokenKinds.NE:
            return VAL_NE;
        case XPathTokenKinds.EQ:
            return VAL_EQ;
        case XPathTokenKinds.LT:
            return VAL_LT;
        case XPathTokenKinds.LE:
            return VAL_LE;
        case XPathTokenKinds.GT:
            return VAL_GT;
        case XPathTokenKinds.GE:
            return VAL_GE;

        case XPathTokenKinds.DOUBLE_PERIOD:
            return RANGE;

        case XPathTokenKinds.DOUBLE_PIPE:
            return STRING_CONCAT;

        default:
            throw new IllegalArgumentException(XPathTokenKinds.describe(kind) + " is no valid operator kind");
        }
    }
}
