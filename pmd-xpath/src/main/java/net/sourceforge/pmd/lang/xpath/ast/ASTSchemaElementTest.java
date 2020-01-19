/*
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

    /** Constructor for synthetic node. */
    public ASTSchemaElementTest() {
        super(XPathParserImplTreeConstants.JJTSCHEMAELEMENTTEST);
    }

    ASTSchemaElementTest(int id) {
        this();
    }


    /**
     * Gets the node representing the name of the tested element.
     */
    public ASTName getElementNameNode() {
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
