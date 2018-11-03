/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Comparison expression.
 *
 * <pre>
 *
 * ComparisonExpr ::=  {@linkplain ASTStringConcatExpr StringConcatExpr} {@link ComparisonOperator} {@linkplain ASTStringConcatExpr StringConcatExpr}
 *
 * </pre>
 */
public final class ASTComparisonExpr extends AbstractXPathNode implements ExprSingle {

    private String operator;


    /** Constructor for synthetic node. */
    public ASTComparisonExpr() {
        super(null, XPathParserTreeConstants.JJTCOMPARISONEXPR);
    }


    ASTComparisonExpr(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets an enum constant representing the operator.
     */
    public ComparisonOperator getOperator() {
        return ComparisonOperator.getConstant(operator);
    }


    /**
     * Gets the image of the operator.
     */
    public String getOperatorImage() {
        return operator;
    }


    /**
     * Gets the left hand side of the comparison.
     */
    public Expr getLhs() {
        return (Expr) jjtGetChild(0);
    }


    /**
     * Gets the right hand side of the comparison.
     */
    public Expr getRhs() {
        return (Expr) jjtGetChild(1);
    }


    void setOperator(String s) {
        operator = s;
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Comparison operator in a {@linkplain ASTComparisonExpr ComparisonExpr}.
     *
     * <pre>
     *
     * (: Not a node :)
     * ComparisonOperator ::= ValueComp | GeneralComp | NodeComp
     *
     * ValueComp    ::= "eq" | "ne" | "lt" | "le" | "gt" | "ge"
     * GeneralComp  ::= "=" | "!=" | "<" | "<=" | ">" | ">="
     * NodeComp     ::= "is" | "<<" | ">>"
     *
     * </pre>
     */
    public enum ComparisonOperator {
        // ValueComp

        /** Operator "=". */
        G_EQUALS("="),
        /** Operator "!=". */
        G_NOT_EQUALS("!="),
        /** Operator "<". */
        G_LOWER("<"),
        /** Operator "<=". */
        G_LOWER_EQUALS("<="),
        /** Operator ">". */
        G_GREATER(">"),
        /** Operator ">=". */
        G_GREATER_EQUALS(">="),

        // GeneralComp
        /** Operator "eq". */
        V_EQ("eq"),
        /** Operator "ne". */
        V_NE("ne"),
        /** Operator "lt". */
        V_LT("lt"),
        /** Operator "le". */
        V_LE("le"),
        /** Operator "gt". */
        V_GT("gt"),
        /** Operator "ge". */
        V_GE("ge"),

        // NodeComp
        /** Operator "<<". */
        N_PRECEDES("<<"),
        /** Operator ">>". */
        N_FOLLOWS(">>"),
        /** Operator "is". */
        N_IDENTITY("is");

        private static final Map<String, ComparisonOperator> IMAGE_TO_OP;

        static {
            Map<String, ComparisonOperator> tmp = new HashMap<>();

            for (ComparisonOperator op : values()) {
                tmp.put(op.getImage(), op);
            }
            IMAGE_TO_OP = Collections.unmodifiableMap(tmp);
        }

        private final String image;


        ComparisonOperator(String image) {
            this.image = image;
        }


        /**
         * Returns the image of the operator as it appears in the source.
         */
        public String getImage() {
            return image;
        }


        /**
         * Returns true if this operator is a node comparison operator.
         * Node comparisons are used to compare two nodes, by their
         * identity or by their document order.
         */
        public boolean isNodeComp() {
            return name().startsWith("N_");
        }


        /**
         * Returns true if this operator is a general comparison operator.
         * The general comparison operators are =, !=, <, <=, >, and >=.
         * General comparisons are existentially quantified comparisons that
         * may be applied to operand sequences of any length. The result of a
         * general comparison that does not raise an error is always true or false.
         */
        public boolean isGeneralComp() {
            return name().startsWith("G_");
        }


        /**
         * Returns true if this operator is a value comparison operator.
         * The value comparison operators are eq, ne, lt, le, gt, and ge. Value
         * comparisons are used for comparing single values.
         */
        public boolean isValueComp() {
            return name().startsWith("V_");
        }


        /**
         * Gets an operator from its image.
         */
        public static ComparisonOperator getConstant(String image) {
            return IMAGE_TO_OP.get(image);
        }

    }
}
/* JavaCC - OriginalChecksum=6671255ce9211f381c3824ae0513527c (do not edit this line) */
