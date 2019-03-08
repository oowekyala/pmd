/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Comment kind test.
 *
 * <pre>
 *
 * CommentTest ::= "comment" "(" ")"
 *
 * </pre>
 */
public final class ASTCommentTest extends AbstractXPathNode implements KindTest {

    /** Constructor for synthetic node. */
    public ASTCommentTest() {
        super(null, XPathParserTreeConstants.JJTCOMMENTTEST);
    }


    ASTCommentTest(XPathParser p, int id) {
        super(p, id);
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
/* JavaCC - OriginalChecksum=b64ec512ea4882f31a21aa8f85a8a2dc (do not edit this line) */
