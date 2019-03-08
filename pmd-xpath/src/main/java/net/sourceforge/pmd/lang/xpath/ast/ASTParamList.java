/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Iterator;
import javax.annotation.Nullable;


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

    /** Constructor for synthetic node. */
    public ASTParamList() {
        super(null, XPathParserTreeConstants.JJTPARAMLIST);
    }


    ASTParamList(XPathParser p, int id) {
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


    @Override
    public Iterator<ASTParam> iterator() {
        return new NodeChildrenIterator<>(this, ASTParam.class);
    }
}
/* JavaCC - OriginalChecksum=ff1a41543bd1e7d1dfa727bb341faa09 (do not edit this line) */
