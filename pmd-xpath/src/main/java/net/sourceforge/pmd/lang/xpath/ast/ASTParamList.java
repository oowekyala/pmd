/*
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

    /** Constructor for synthetic node. */
    public ASTParamList() {
        super(XPathParserImplTreeConstants.JJTPARAMLIST);
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
    public Iterator<ASTParam> iterator() {
        return children(ASTParam.class).iterator();
    }
}
