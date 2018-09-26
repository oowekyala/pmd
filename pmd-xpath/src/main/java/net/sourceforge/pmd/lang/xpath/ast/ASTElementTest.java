/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;
import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest.ElementTestOrSchemaElementTest;

// @formatter:off
/**
 * Element kind test. See https://www.w3.org/TR/xpath-30/#doc-xpath30-ElementTest
 *
 *
 * <p>Examples:
 * <ul>
 *     <li>element()
 *     <li>element(*)
 *     <li>element(person)
 *     <li>element(person, surgeon)
 *     <li>element(person, surgeon?)
 *     <li>element(*, surgeon)
 *     <li>element(*, surgeon?)
 * </ul>
 *
 *
 * <pre>
 *
 * ElementTest ::=  "element" "(" (
 *                                ( {@linkplain ASTName ElementName} | "*" )
 *                                ( "," {@linkplain ASTName TypeName} ("?")? )?
 *                                )?
 *                            ")"
 *
 * </pre>
 *
 */
// @formatter:on
public final class ASTElementTest extends AbstractXPathNode implements KindTest, ElementTestOrSchemaElementTest {

    /** Constructor for synthetic node. */
    ASTElementTest() {
        super(null, XPathParserTreeConstants.JJTELEMENTTEST);
    }

    // null means absent
    private Boolean isWildcard = null;
    private boolean isOptionalType;


    ASTElementTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if the type name is qualified with a "?",
     * false in all other cases.
     */
    public boolean isOptionalType() {
        return isOptionalType;
    }


    void setOptionalType(boolean optionalType) {
        isOptionalType = optionalType;
    }


    void setIsWildcard(boolean value) {
        isWildcard = value;
    }


    /**
     * Returns true if this is of the form element(),
     * false in all other cases.
     */
    public boolean isEmptyParen() {
        return isWildcard == null;
    }


    /**
     * Returns null if this kind test is of the form:
     * <ul>
     * <li>element()
     * <li>element(*)
     * <li>element(*, surgeon)
     * <li>element(*, surgeon?)
     * </ul>
     *
     * Otherwise returns the element name.
     */
    public ASTName getElementName() {
        return isWildcard == null || isWildcard
               ? null
               : (ASTName) jjtGetChild(0);
    }


    /**
     * Returns the type name if it is present. {@literal i.e.} returns a non-null
     * value if this test is of the form:
     * <ul>
     * <li>element(person, surgeon)
     * <li>element(person, surgeon?)
     * <li>element(*, surgeon)
     * <li>element(*, surgeon?)
     * </ul>
     *
     * Otherwise returns null.
     */
    public ASTName getTypeName() {
        if (getElementName() == null && jjtGetNumChildren() == 1) {
            return (ASTName) jjtGetChild(0);
        } else if (getElementName() != null && jjtGetNumChildren() == 2) {
            return (ASTName) jjtGetChild(1);
        } else {
            return null;
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
/* JavaCC - OriginalChecksum=77aa327f82e35f535bdae68879b9dc27 (do not edit this line) */
