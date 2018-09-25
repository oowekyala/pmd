/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

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


    ASTAnyItemType(XPathParser p, int id) {
        super(p, id);
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
/* JavaCC - OriginalChecksum=6411fea1ca8a1382af9466f9eb3abb57 (do not edit this line) */
