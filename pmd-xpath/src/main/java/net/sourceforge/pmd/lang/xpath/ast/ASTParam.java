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

    // TODO synthesize default type?


    ASTParam(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of the variable.
     */
    public ASTName getNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=6b7690eb5f46d382c13c36defb497b11 (do not edit this line) */
