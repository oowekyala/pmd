/**
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
        super(null, XPathParserTreeConstants.JJTARGUMENTTYPELIST);
    }


    ASTArgumentTypeList(XPathParser p, int id) {
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
    public Iterator<ASTSequenceType> iterator() {
        return new NodeChildrenIterator<>(this, ASTSequenceType.class);
    }
}
/* JavaCC - OriginalChecksum=275fa73d266d8c87073e31ffd8e85f3f (do not edit this line) */
