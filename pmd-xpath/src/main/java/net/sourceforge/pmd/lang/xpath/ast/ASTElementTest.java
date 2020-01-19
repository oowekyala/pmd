/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import org.checkerframework.checker.nullness.qual.Nullable;

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
    public ASTElementTest() {
        super(XPathParserImplTreeConstants.JJTELEMENTTEST);
    }

    // null means absent
    private Boolean isWildcard = null;
    private boolean isOptionalType;


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
     * Returns an empty optional if this kind test is of the form:
     * <ul>
     * <li>element()
     * <li>element(*)
     * <li>element(*, surgeon)
     * <li>element(*, surgeon?)
     * </ul>
     *
     * Otherwise returns the element name.
     */
    @Nullable
    public ASTName getElementName() {
        return isWildcard == null || isWildcard
               ? null
               : (ASTName) getChild(0);
    }


    /**
     * Returns the type name if it is present. {@literal i.e.} returns a non-empty
     * value if this test is of the form:
     * <ul>
     * <li>element(person, surgeon)
     * <li>element(person, surgeon?)
     * <li>element(*, surgeon)
     * <li>element(*, surgeon?)
     * </ul>
     *
     * Otherwise returns an empty optional.
     */
    @Nullable
    public ASTName getTypeName() {
        if (getElementName() == null && getNumChildren() == 1) {
            return (ASTName) getChild(0);
        } else if (getElementName() != null && getNumChildren() == 2) {
            return (ASTName) getChild(1);
        } else {
            return null;
        }
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
