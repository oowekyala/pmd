/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


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
        super(XPathParserImplTreeConstants.JJTSCHEMAATTRIBUTETEST);
    }


    /**
     * Gets the node representing the name of the tested attribute.
     */
    public ASTName getAttributeNameNode() {
        return (ASTName) getChild(0);
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
