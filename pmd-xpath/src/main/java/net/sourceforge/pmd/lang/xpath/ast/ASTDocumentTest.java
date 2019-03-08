/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

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

    /** Constructor for synthetic node. */
    public ASTDocumentTest() {
        super(null, XPathParserTreeConstants.JJTDOCUMENTTEST);
    }


    ASTDocumentTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the argument kind test, or null if there is none.
     */
    @Nullable
    public ElementTestOrSchemaElementTest getArgumentTest() {
        return jjtGetNumChildren() == 0 ? null : (ElementTestOrSchemaElementTest) jjtGetChild(0);
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
/* JavaCC - OriginalChecksum=83dc39618e2fa96fb72e304333aa5cac (do not edit this line) */
