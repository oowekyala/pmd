/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

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
        super(XPathParserImplTreeConstants.JJTPARENTHESIZEDITEMTYPE);
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
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
/* JavaCC - OriginalChecksum=4d0e9b33e4c4e44319aeba3f167fb764 (do not edit this line) */
