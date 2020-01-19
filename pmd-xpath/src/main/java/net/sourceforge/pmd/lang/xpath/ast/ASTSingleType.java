/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Single type node.
 *
 * <pre>
 *
 * SingleType ::= {@linkplain ASTName SimpleTypeName} ("?")?
 *
 * </pre>
 */
public final class ASTSingleType extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTSingleType() {
        super(XPathParserImplTreeConstants.JJTSINGLETYPE);
    }

    private boolean optional;


    void setOptionallyQuantified() {
        optional = true;
    }


    boolean isOptional() {
        return optional;
    }


    /**
     * Gets the node representing the type name.
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
