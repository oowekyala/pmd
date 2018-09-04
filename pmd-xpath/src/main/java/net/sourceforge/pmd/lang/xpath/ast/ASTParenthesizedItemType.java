/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

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


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(SideEffecting<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=4d0e9b33e4c4e44319aeba3f167fb764 (do not edit this line) */
