/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Parenthesized item type.
 *
 * <pre>
 *
 * ParenthesizedItemType ::= "(" {@link ItemType} ")"
 *
 * </pre>
 */
public final class ASTParenthesizedItemType extends AbstractXPathNode implements ItemType, ParenthesizedNode<ItemType> {

    /** Constructor for synthetic node. */
    public ASTParenthesizedItemType() {
        super(null, XPathParserTreeConstants.JJTPARENTHESIZEDITEMTYPE);
    }


    ASTParenthesizedItemType(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the expression wrapped in the parentheses.
     */
    @Override
    public ItemType getWrappedNode() {
        return (ItemType) jjtGetChild(0);
    }


    @Nullable
    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }
}
/* JavaCC - OriginalChecksum=4d0e9b33e4c4e44319aeba3f167fb764 (do not edit this line) */
