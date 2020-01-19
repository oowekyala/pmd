/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Test that matches any kind.
 *
 * <pre>
 *
 * AnyKindTest ::= "node" "(" ")"
 *
 * </pre>
 */
public final class ASTAnyKindTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    public ASTAnyKindTest() {
        super(XPathParserImplTreeConstants.JJTANYKINDTEST);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
