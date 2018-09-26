/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.ItemType.FunctionTest;


/**
 * Function test that matches anything.
 *
 * <pre>
 *
 * AnyFunctionTest ::= "function" "(" "*" ")"
 *
 * </pre>
 */
public final class ASTAnyFunctionTest extends AbstractXPathNode implements FunctionTest {

    /** Constructor for synthetic node. */
    ASTAnyFunctionTest() {
        super(null, XPathParserTreeConstants.JJTANYFUNCTIONTEST);
    }


    ASTAnyFunctionTest(XPathParser p, int id) {
        super(p, id);
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
/* JavaCC - OriginalChecksum=929b2e4e9c2ecfbb6d638230bd82a085 (do not edit this line) */
