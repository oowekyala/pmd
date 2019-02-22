/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Context item expression.
 *
 * <pre>
 *
 * ContextItemExpr ::= "."
 *
 * </pre>
 */
public final class ASTContextItemExpr extends AbstractXPathNode implements PrimaryExpr {

    /** Constructor for synthetic node. */
    public ASTContextItemExpr() {
        super(null, XPathParserTreeConstants.JJTCONTEXTITEMEXPR);
    }


    ASTContextItemExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


}
/* JavaCC - OriginalChecksum=f84ce10a4a9a29e0d18b17f6a11e34f8 (do not edit this line) */