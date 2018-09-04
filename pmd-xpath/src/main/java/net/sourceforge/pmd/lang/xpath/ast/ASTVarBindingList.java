/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * List of variable bindings in a {@linkplain ASTLetExpr let expression},
 * {@linkplain ASTForExpr for expression}, or {@linkplain ASTQuantifiedExpr quantified expression}.
 *
 * <pre>
 *
 * VarBindingList ::= {@linkplain ASTVarBinding VarBinding} ( "," {@linkplain ASTVarBinding VarBinding} )
 *
 * </pre>
 */
public final class ASTVarBindingList extends AbstractXPathNode implements Iterable<ASTVarBinding> {


    ASTVarBindingList(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<ASTVarBinding> iterator() {
        return new NodeChildrenIterator<>(this, ASTVarBinding.class);
    }
}
/* JavaCC - OriginalChecksum=efe89d2f2ae923cbfde45c7944632a72 (do not edit this line) */
