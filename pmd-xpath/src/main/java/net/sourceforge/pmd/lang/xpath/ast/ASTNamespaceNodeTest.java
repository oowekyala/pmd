/**
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


    ASTNamespaceNodeTest(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=2ca63014f2dd56c9afb62540fb273054 (do not edit this line) */
