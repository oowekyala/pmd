/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Sequence expression. The comma separated expressions compose a sequence.
 * The comma operator has the lowest priority of all, and this expression is
 * forbidden in some contexts unless it's parenthesized.
 *
 * <pre>
 *
 * SequenceExpr ::= {@link Expr} ("," {@link Expr})+
 *
 * </pre>
 */
public final class ASTSequenceExpr extends AbstractXPathExpr implements Iterable<Expr>, Expr, SequenceExpr {

    /** Constructor for synthetic node. */
    public ASTSequenceExpr(List<? extends Expr> elts) {
        super(XPathParserImplTreeConstants.JJTSEQUENCEEXPR);

        if (elts.isEmpty()) {
            throw new IllegalArgumentException("ASTSequenceExpr cannot represent empty sequence");
        }

        children = new Node[elts.size()];

        int i = 0;
        for (Expr elt : elts) {
            insertSyntheticChild(elt, i++);
        }
    }

    public ASTSequenceExpr(int id) {
        super(XPathParserImplTreeConstants.JJTSEQUENCEEXPR);

    }


    @Override
    public int getSize() {
        return getNumChildren();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<Expr> iterator() {
        return children(Expr.class).iterator();
    }
}
