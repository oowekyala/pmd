/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;


/**
 * Empty sequence expression.
 *
 * <pre>
 *
 * EmptySequenceExpr ::= "(" ")"
 *
 * </pre>
 */
public final class ASTEmptySequenceExpr extends AbstractXPathNode implements ExprSingle, SequenceExpr {


    /** Constructor for synthetic node. */
    public ASTEmptySequenceExpr() {
        super(null, XPathParserTreeConstants.JJTEMPTYSEQUENCEEXPR);
    }


    ASTEmptySequenceExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public int getSize() {
        return 0;
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


    @Override
    public Iterator<ExprSingle> iterator() {
        return Collections.emptyIterator();
    }
}
/* JavaCC - OriginalChecksum=2e2c123dc1554f24119210ce5dedcec4 (do not edit this line) */
