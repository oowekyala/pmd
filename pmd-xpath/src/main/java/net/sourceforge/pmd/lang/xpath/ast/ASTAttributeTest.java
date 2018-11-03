/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Optional;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Attribute test.
 *
 * <p>Examples:
 * <ul>
 * <li>attribute()
 * <li>attribute(*)
 * <li>attribute(price)
 * <li>attribute(price, currency)
 * <li>attribute(*, currency)
 * </ul>
 *
 * <pre>
 *
 * AttributeTest ::= "attribute" "(" (
 *                                     ( {@linkplain ASTName AttributeName} | "*" )
 *                                     ( "," {@linkplain ASTName TypeName})?
 *                                   )?
 *                                ")"
 *
 * </pre>
 */
public final class ASTAttributeTest extends AbstractXPathNode implements KindTest {

    // null means absent
    private Boolean isWildcard = null;


    /** Constructor for synthetic node. */
    public ASTAttributeTest() {
        super(null, XPathParserTreeConstants.JJTATTRIBUTETEST);
    }


    ASTAttributeTest(XPathParser p, int id) {
        super(p, id);
    }


    void setIsWildcard(boolean value) {
        isWildcard = value;
    }


    /**
     * Returns true if this is of the form attribute(),
     * false in all other cases.
     */
    public boolean isEmptyParen() {
        return isWildcard == null;
    }


    /**
     * Returns the attribute name. Returns null if this kind test is of the form:
     * <ul>
     * <li>attribute()
     * <li>attribute(*)
     * <li>attribute(*, currency)
     * </ul>
     */
    public Optional<ASTName> getAttributeName() {
        return isWildcard == null || isWildcard
               ? Optional.empty()
               : Optional.of((ASTName) jjtGetChild(0));
    }


    /**
     * Returns the type name if it is present. {@literal i.e.} returns a non-null
     * value if this test is of the form:
     * <ul>
     * <li>attribute(price, currency)
     * <li>attribute(*, currency)
     * </ul>
     *
     * Otherwise returns null.
     */
    public Optional<ASTName> getTypeName() {
        if (!getAttributeName().isPresent() && jjtGetNumChildren() == 1) {
            return Optional.of((ASTName) jjtGetChild(0));
        } else if (getAttributeName().isPresent() && jjtGetNumChildren() == 2) {
            return Optional.of((ASTName) jjtGetChild(1));
        } else {
            return Optional.empty();
        }
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
}
/* JavaCC - OriginalChecksum=ff3f1f36406fc4c537672c556b59ef93 (do not edit this line) */
