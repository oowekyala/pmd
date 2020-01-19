/*
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

    ASTParenthesizedItemType(int id) {
        this();
    }


    /**
     * Gets the expression wrapped in the parentheses.
     */
    @Override
    public ItemType getWrappedNode() {
        return (ItemType) getChild(0);
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
