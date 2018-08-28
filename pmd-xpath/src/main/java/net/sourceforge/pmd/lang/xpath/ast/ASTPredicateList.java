/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * A possibly empty list of predicates attached to an {@linkplain ASTAxisStep axis step}.
 */
public final class ASTPredicateList extends AbstractXPathNode implements Iterable<ASTExpr> {


    ASTPredicateList(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<ASTExpr> iterator() {
        return new NodeChildrenIterator<>(this, ASTExpr.class);
    }
}
/* JavaCC - OriginalChecksum=16267cf146053711e8c1f34d511e85d3 (do not edit this line) */
