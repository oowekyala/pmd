/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;


/**
 * Parameter list of an {@linkplain ASTInlineFunctionExpr InlineFunctionExpr}.
 *
 * <pre>
 *
 * ParamList ::= "(" ( {@linkplain ASTParam Param} ("," {@linkplain ASTParam Param} )* )? ")"
 *
 * </pre>
 */
public final class ASTParamList extends AbstractXPathNode implements Iterable<ASTParam> {


    ASTParamList(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<ASTParam> iterator() {
        return new NodeChildrenIterator<>(this, ASTParam.class);
    }
}
/* JavaCC - OriginalChecksum=ff1a41543bd1e7d1dfa727bb341faa09 (do not edit this line) */
