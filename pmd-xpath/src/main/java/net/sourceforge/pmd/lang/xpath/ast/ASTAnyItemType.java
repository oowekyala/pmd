/*
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

    /** Constructor for synthetic node. */
    public ASTAnyItemType() {
        super(XPathParserImplTreeConstants.JJTANYITEMTYPE);
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
