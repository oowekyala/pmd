/**
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
    ASTAtomicOrUnionType() {
        super(null, XPathParserTreeConstants.JJTATOMICORUNIONTYPE);
    }


    ASTAtomicOrUnionType(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the type name.
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
/* JavaCC - OriginalChecksum=f8116dc05dbbbc73b3879482a82a629e (do not edit this line) */
