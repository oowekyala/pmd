/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;


/**
 * Argument type list occurring in a {@linkplain ASTTypedFunctionTest TypedFunctionTest}.
 *
 * <pre>
 *
 * ArgumentTypeList ::=  "(" ( {@linkplain ASTSequenceType SequenceType} ( "," {@linkplain ASTSequenceType SequenceType} )* )? ")"
 *
 * </pre>
 */
public final class ASTArgumentTypeList extends AbstractXPathNode implements Iterable<ASTSequenceType> {

    /** Constructor for synthetic node. */
    public ASTArgumentTypeList() {
        super(XPathParserImplTreeConstants.JJTARGUMENTTYPELIST);
    }

    ASTArgumentTypeList(int id) {
        this();
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
    public Iterator<ASTSequenceType> iterator() {
        return children(ASTSequenceType.class).iterator();
    }
}
