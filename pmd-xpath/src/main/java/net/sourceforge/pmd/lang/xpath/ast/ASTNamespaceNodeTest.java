/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Namespace node test.
 *
 * <pre>
 *
 * NamespaceNodeTest ::= "namespace-node" "(" ")"
 *
 * </pre>
 */
public final class ASTNamespaceNodeTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    public ASTNamespaceNodeTest() {
        super(XPathParserImplTreeConstants.JJTNAMESPACENODETEST);
    }

    ASTNamespaceNodeTest(int id) {
        this();
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
