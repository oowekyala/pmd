/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


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
        super(XPathParserImplTreeConstants.JJTCOMMENTTEST);
    }

    ASTCommentTest(int id) {
        this();
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
