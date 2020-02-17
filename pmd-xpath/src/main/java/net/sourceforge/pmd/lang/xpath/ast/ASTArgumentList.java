/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;

import net.sourceforge.pmd.lang.ast.NodeStream;


/**
 * List of arguments of a {@link ASTFunctionCall function call}.
 *
 * <pre>
 *
 * ArgumentsList ::= "(" ({@link ASTArgument Argument} ("," {@link ASTArgument Argument} )*)? ")"
 *
 * </pre>
 */
public final class ASTArgumentList extends AbstractXPathNode implements Iterable<ASTArgument> {

    /** Constructor for synthetic node. */
    public ASTArgumentList() {
        super(XPathParserImplTreeConstants.JJTARGUMENTLIST);
    }

    public int getArgumentNumber() {
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
    public NodeStream<ASTArgument> children() {
        return children(ASTArgument.class);
    }

    @Override
    public Iterator<ASTArgument> iterator() {
        return children().iterator();
    }
}
