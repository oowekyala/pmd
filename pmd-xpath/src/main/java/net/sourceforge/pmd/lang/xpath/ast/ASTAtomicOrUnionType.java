/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Atomic or union type.
 *
 * <pre>
 *
 * AtomicOrUnionType ::= {@linkplain ASTName EQName}
 *
 * </pre>
 */
public final class ASTAtomicOrUnionType extends AbstractXPathNode implements ItemType {

    /** Constructor for synthetic node. */
    public ASTAtomicOrUnionType() {
        super(XPathParserImplTreeConstants.JJTATOMICORUNIONTYPE);
    }

    ASTAtomicOrUnionType(int id) {
        this();
    }


    /**
     * Returns the type name.
     */
    public ASTName getTypeNameNode() {
        return (ASTName) getChild(0);
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
