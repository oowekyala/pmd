/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Processing instruction kind test.
 *
 * <pre>
 *
 * ProcessingInstructionTest ::= "processing-instruction" "(" ({@linkplain ASTName NcName} | {@linkplain ASTStringLiteral StringLiteral})? ")"
 *
 * </pre>
 */
public final class ASTProcessingInstructionTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    public ASTProcessingInstructionTest() {
        super(null, XPathParserTreeConstants.JJTPROCESSINGINSTRUCTIONTEST);
    }


    ASTProcessingInstructionTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if test specifies the name of a specific PI.
     */
    public boolean hasArgument() {
        return jjtGetNumChildren() > 0;
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
/* JavaCC - OriginalChecksum=9736f93d02630e9fe57519649309dc83 (do not edit this line) */
