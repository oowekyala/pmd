/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

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
        super(null, XPathParserTreeConstants.JJTNAMESPACENODETEST);
    }


    ASTNamespaceNodeTest(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }
}
/* JavaCC - OriginalChecksum=2ca63014f2dd56c9afb62540fb273054 (do not edit this line) */
