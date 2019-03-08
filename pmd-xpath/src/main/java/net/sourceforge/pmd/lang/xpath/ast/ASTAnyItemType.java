/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Type denoting any item.
 *
 * <pre>
 *
 * AnyItemType ::= "item" "(" ")"
 *
 * </pre>
 */
public final class ASTAnyItemType extends AbstractXPathNode implements ItemType {

    /** Constructor for synthetic node. */
    public ASTAnyItemType() {
        super(null, XPathParserTreeConstants.JJTANYITEMTYPE);
    }


    ASTAnyItemType(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=6411fea1ca8a1382af9466f9eb3abb57 (do not edit this line) */
