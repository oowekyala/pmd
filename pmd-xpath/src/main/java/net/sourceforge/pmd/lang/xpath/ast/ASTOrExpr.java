/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

/**
 * Boolean OR expression.
 *
 * <pre>
 *
 * OrExpr ::= {@linkplain ASTAndExpr AndExpr} ( "or" {@linkplain ASTAndExpr AndExpr} )+
 *
 * </pre>
 */
public final class ASTOrExpr extends AbstractXPathNode implements LogicalExpr {

    /** Constructor for synthetic node. */
    public ASTOrExpr() {
        super(null, XPathParserTreeConstants.JJTOREXPR);
    }


    ASTOrExpr(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=4ac29a32cf9359e1ab1cc65792b744dd (do not edit this line) */
