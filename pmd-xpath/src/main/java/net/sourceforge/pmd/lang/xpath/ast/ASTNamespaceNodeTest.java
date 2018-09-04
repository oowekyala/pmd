/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


public final class ASTNamespaceNodeTest extends AbstractXPathNode implements KindTest {


    ASTNamespaceNodeTest(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(SideEffecting<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=2ca63014f2dd56c9afb62540fb273054 (do not edit this line) */
