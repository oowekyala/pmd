/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * Sequence expression. Comma separated expressions compose a sequence.
 * This corresponds to the Expr production in the grammar.
 *
 * <pre>
 *
 * SequenceExpr ::= {@link Expr} ("," {@link Expr})+
 *
 * </pre>
 */
public final class ASTSequenceExpr extends AbstractXPathNode implements Iterable<Expr> {


    ASTSequenceExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<Expr> iterator() {
        return new NodeChildrenIterator<>(this, Expr.class);
    }
}
/* JavaCC - OriginalChecksum=2e2c123dc1554f24119210ce5dedcec4 (do not edit this line) */
