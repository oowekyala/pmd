/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;
import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest.ElementTestOrSchemaElementTest;


/**
 * Schema element test.
 *
 * <pre>
 *
 * SchemaElementTest ::= "schema-element" "(" {@linkplain ASTName ElementName} ")"
 *
 * </pre>
 */
public final class ASTSchemaElementTest extends AbstractXPathNode implements KindTest, ElementTestOrSchemaElementTest {


    ASTSchemaElementTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of the tested element.
     */
    public ASTName getElementNameNode() {
        return (ASTName) jjtGetChild(0);
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
}
/* JavaCC - OriginalChecksum=d820a212ba68b42360ab7f30a7b34171 (do not edit this line) */
