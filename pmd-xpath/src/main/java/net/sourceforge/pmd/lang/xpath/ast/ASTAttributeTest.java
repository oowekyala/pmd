/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import org.checkerframework.checker.nullness.qual.Nullable;

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
 *                                     ( {@link ASTName AttributeName} | "*" )
 *                                     ( "," {@link ASTName TypeName})?
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
        super(XPathParserImplTreeConstants.JJTATTRIBUTETEST);
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
    @Nullable
    public ASTName getAttributeName() {
        return isWildcard == null || isWildcard
               ? null
               : (ASTName) getChild(0);
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
    @Nullable
    public ASTName getTypeName() {
        if (getAttributeName() == null && getNumChildren() == 1) {
            return (ASTName) getChild(0);
        } else if (getAttributeName() != null && getNumChildren() == 2) {
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
