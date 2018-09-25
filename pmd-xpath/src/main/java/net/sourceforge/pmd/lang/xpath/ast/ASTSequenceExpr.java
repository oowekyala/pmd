/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * Sequence expression. The comma separated expressions compose a sequence.
 * The comma operator has the lowest priority of all, and this expression is
 * forbidden in some contexts unless it's {@linkplain ASTParenthesizedExpr parenthesized}.
 *
 *
 * <pre>
 *
 * SequenceExpr ::= {@link ExprSingle} ("," {@link ExprSingle})+
 *
 * </pre>
 */
public final class ASTSequenceExpr extends AbstractXPathNode implements Iterable<ExprSingle>, Expr {


    ASTSequenceExpr(XPathParser p, int id) {
        super(p, id);
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


    @Override
    public Iterator<ExprSingle> iterator() {
        return new NodeChildrenIterator<>(this, ExprSingle.class);
    }
}
/* JavaCC - OriginalChecksum=2e2c123dc1554f24119210ce5dedcec4 (do not edit this line) */
