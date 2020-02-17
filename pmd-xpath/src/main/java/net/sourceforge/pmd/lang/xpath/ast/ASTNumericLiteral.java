/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * Numeric literal. XPath numeric literals are of 3 types:
 *
 * <pre>
 *
 *     &lt;INTEGER_LITERAL&gt; ::= "\d+"
 *     &lt;DECIMAL_LITERAL&gt; ::= "\.\d+" | "\d+\.\d*"
 *     &lt;DOUBLE_LITERAL&gt;  ::= (&lt;INTEGER_LITERAL&gt; | &lt;DECIMAL_LITERAL&gt;) ("e" | "E") ("+" | "-")? "\d+"
 *
 * </pre>
 */
public final class ASTNumericLiteral extends AbstractXPathExpr implements PrimaryExpr {

    private Boolean literalType = null;
    private int integerPart;
    private double decimalPart;
    private int exponent;
    private double doubleValue;


    /** Constructor for synthetic node. */
    public ASTNumericLiteral(String image) {
        super(XPathParserImplTreeConstants.JJTNUMERICLITERAL);
        setImage(image);
    }

    ASTNumericLiteral() {
        super(XPathParserImplTreeConstants.JJTNUMERICLITERAL);
    }


    @Override
    public void setImage(String image) {
        super.setImage(image);
        decomposeNumber();
    }

    private void decomposeNumber() {
        String image = getImage();

        // we know the parser matched it so some of these groups aren't empty
        Matcher m = Pattern.compile("([0-9]*+)\\.?([0-9]*+)([eE]([+-]?[0-9]+))?").matcher(image);

        if (m.matches()) {
            if (StringUtils.isNotBlank(m.group(1))) {
                integerPart = Integer.valueOf(m.group(1));
            }

            if (StringUtils.isNotBlank(m.group(2))) {
                decimalPart = Double.valueOf("." + m.group(2));
            }

            if (StringUtils.isNotBlank(m.group(3))) {
                exponent = Integer.valueOf(m.group(4));
            }

        } else {
            throw new IllegalStateException("Malformed numeric literal or procedure doesn't work");
        }

        doubleValue = (integerPart + decimalPart) * Math.pow(10, exponent);
    }


    /**
     * Returns the {@link #getDoubleValue() double value} coerced
     * into an int.
     */
    public int getIntValue() {
        return isIntegerLiteral() ? integerPart : (int) doubleValue;
    }


    /**
     * Returns the computed value of the literal.
     */
    public double getDoubleValue() {
        return doubleValue;
    }


    public boolean isIntegerLiteral() {
        return literalType == null;
    }


    public boolean isDecimalLiteral() {
        return literalType == Boolean.TRUE;
    }


    public boolean isDoubleLiteral() {
        return literalType == Boolean.FALSE;
    }


    void setDoubleLiteral() {
        literalType = Boolean.FALSE;
    }


    void setIntegerLiteral() {
        literalType = null;
    }


    void setDecimalLiteral() {
        literalType = Boolean.TRUE;
    }


    /**
     * Returns the integer part, is zero in {@literal e.g.} {@code 0.2} or {@code .2}.
     */
    public int getIntegerPart() {
        return integerPart;
    }


    /**
     * Returns the decimal part, is zero in {@literal e.g.} {@code 12} or {@code 22.}.
     */
    public double getDecimalPart() {
        return decimalPart;
    }


    /**
     * Returns the exponent, is zero in {@literal e.g.} {@code 0.2e0} or {@code 222}.
     */
    public int getExponent() {
        return exponent;
    }


    /**
     * Returns the literal as it appeared in the source.
     */
    @Override
    public String getImage() { // NOPMD
        return super.getImage();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
