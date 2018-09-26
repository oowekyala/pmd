/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Text kind test.
 *
 * <pre>
 *
 * CommentTest ::= "text" "(" ")"
 *
 * </pre>
 */
public final class ASTTextTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    ASTTextTest() {
        super(null, XPathParserTreeConstants.JJTTEXTTEST);
    }


    ASTTextTest(XPathParser p, int id) {
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
/* JavaCC - OriginalChecksum=7a8ca09c25217e381bdc126c59b3eae6 (do not edit this line) */
