/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Schema attribute test.
 *
 *
 * <pre>
 *
 * SchemaAttributeTest ::= "schema-attribute" "(" {@linkplain ASTName AttributeName} ")"
 *
 * </pre>
 */
public final class ASTSchemaAttributeTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    public ASTSchemaAttributeTest() {
        super(null, XPathParserTreeConstants.JJTSCHEMAATTRIBUTETEST);
    }

    ASTSchemaAttributeTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of the tested attribute.
     */
    public ASTName getAttributeNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=369024f8e9209fc5c8104ac8efef824d (do not edit this line) */
