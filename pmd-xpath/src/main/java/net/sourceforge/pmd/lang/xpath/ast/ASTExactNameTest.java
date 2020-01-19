/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.NameTest;


/**
 * A {@linkplain NameTest name test} that matches nodes having
 * exactly a certain name.
 *
 * <pre>
 *
 * ExactNameTest ::= {@linkplain ASTName Name}
 *
 * </pre>
 */
public final class ASTExactNameTest extends AbstractXPathNode implements NameTest {

    /** Constructor for synthetic node. */
    public ASTExactNameTest() {
        super(XPathParserImplTreeConstants.JJTEXACTNAMETEST);
    }


    /**
     * Returns the image of the name tested for.
     */
    public String getNameImage() {
        return getNameNode().getImage();
    }


    /**
     * Returns the node representing the name tested for.
     */
    public ASTName getNameNode() {
        return (ASTName) jjtGetChild(0);
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
/* JavaCC - OriginalChecksum=f84ce10a4a9a29e0d18b17f6a11e34f8 (do not edit this line) */
