/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import org.apache.commons.lang3.StringEscapeUtils;


/**
 * A string literal, one of the {@linkplain PrimaryExpr primary expressions}.
 * Strings may be delimited by either single or double quotes. Within a literal,
 * the delimiter may be escaped by doubling it.
 *
 * <pre>
 *
 * StringLiteral ::= &lt;STRING_LITERAL&gt;
 *
 * </pre>
 */
public final class ASTStringLiteral extends AbstractXPathNode implements PrimaryExpr {

    private String value;


    ASTStringLiteral(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public void jjtClose() {
        super.jjtClose();

        setImage(jjtGetFirstToken().getImage());

        if (getImage() == null || getImage().length() < 2) {
            throw new IllegalStateException("Malformed string literal!");
        }

        String delim = String.valueOf(getDelimiter());

        this.value = getImage().substring(1, getImage().length() - 1)
                               .replaceAll(delim + delim, delim);
    }


    /**
     * Returns the image of the string as it appeared in the source,
     * including delimiters, escapes, etc.
     */
    @Override
    public String getImage() { // NOPMD
        return super.getImage();
    }


    /**
     * Returns the delimiter of the string.
     */
    public char getDelimiter() {
        return getImage().charAt(0);
    }


    /**
     * Returns the value without delimiters and with unescaped delimiters.
     */
    public String getUnescapedValue() {
        return value;
    }


    /**
     * Returns the {@linkplain #getUnescapedValue() unescaped value} with
     * additional XML unescaping.
     */
    public String getXmlUnescapedValue() {
        return StringEscapeUtils.unescapeXml(value); // deprecated because now in commons-text
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=98f7aaa4be4b56badb9f2abeb228cb00 (do not edit this line) */
