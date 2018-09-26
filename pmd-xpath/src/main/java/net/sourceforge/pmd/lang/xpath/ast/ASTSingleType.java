/**
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
        super(null, XPathParserTreeConstants.JJTSINGLETYPE);
    }

    private boolean optional;


    ASTSingleType(XPathParser p, int id) {
        super(p, id);
    }


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
        return (ASTName) jjtGetChild(0);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=503098c0ce5f4526907d74af03418b35 (do not edit this line) */
