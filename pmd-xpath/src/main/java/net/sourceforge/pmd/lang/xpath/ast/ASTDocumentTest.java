/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Document node kind test.
 *
 * <pre>
 *
 * DocumentTest ::= "document-node" "(" ({@linkplain ASTElementTest ElementTest} | {@linkplain ASTSchemaElementTest SchemaElementTest})? ")"
 *
 * </pre>
 */
public final class ASTDocumentTest extends AbstractXPathNode implements KindTest {


    ASTDocumentTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the argument kind test, or null if there is none.
     */
    public ElementTestOrSchemaElementTest getArgumentTest() {
        return jjtGetNumChildren() == 0 ? null : (ElementTestOrSchemaElementTest) jjtGetChild(0);
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
/* JavaCC - OriginalChecksum=83dc39618e2fa96fb72e304333aa5cac (do not edit this line) */
