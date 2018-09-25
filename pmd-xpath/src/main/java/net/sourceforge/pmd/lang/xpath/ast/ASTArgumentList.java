/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;


/**
 * List of arguments of a {@linkplain ASTFunctionCall function call}.
 *
 * <pre>
 *
 * ArgumentsList ::= "(" ({@linkplain ASTArgument Argument} ("," {@linkplain ASTArgument Argument} )*)? ")"
 *
 * </pre>
 */
public final class ASTArgumentList extends AbstractXPathNode implements Iterable<ASTArgument> {


    ASTArgumentList(XPathParser p, int id) {
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
    public Iterator<ASTArgument> iterator() {
        return new NodeChildrenIterator<>(this, ASTArgument.class);
    }
}
/* JavaCC - OriginalChecksum=fd6d075b8d6ce0f018e80673e3944af3 (do not edit this line) */
