/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Parameter of an {@linkplain ASTInlineFunctionExpr InlineFunctionExpr}.
 * Wrapped in a {@link ASTParamList}.
 *
 * <p>Each parameter has a name and an optional type. If no type is specified,
 * its default type is item()*.
 *
 * <pre>
 *
 * Param ::= "$" {@linkplain ASTName EQName} ( "as" {@linkplain ASTSequenceType SequenceType} )?
 *
 * </pre>
 */
public final class ASTParam extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTParam() {
        super(null, XPathParserTreeConstants.JJTPARAM);
    }


    ASTParam(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of the variable.
     */
    public ASTName getNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    /**
     * Returns true if this parameter has no type annotation,
     * in which case {@code item()*} is assumed.
     */
    public boolean isDefaultType() {
        return jjtGetNumChildren() == 1;
    }

    // TODO synthesize default type node?


    /**
     * Returns the declared type of the parameter,
     * or null if the default is used.
     */
    public ASTSequenceType getDeclaredType() {
        return isDefaultType() ? null : (ASTSequenceType) jjtGetChild(1);
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
/* JavaCC - OriginalChecksum=6b7690eb5f46d382c13c36defb497b11 (do not edit this line) */
