/*
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
        super(XPathParserImplTreeConstants.JJTPROCESSINGINSTRUCTIONTEST);
    }

    ASTProcessingInstructionTest(int id) {
        this();
    }


    /**
     * Returns true if test specifies the name of a specific PI.
     */
    public boolean hasArgument() {
        return getNumChildren() > 0;
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
