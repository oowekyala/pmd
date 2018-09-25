/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.ast.RootNode;


/**
 * Root node of all XPath trees. Always has a unique child.
 */
public final class ASTXPathRoot extends AbstractXPathNode implements RootNode {


    ASTXPathRoot(XPathParser p, int id) {
        super(p, id);
    }


    public Expr getMainExpr() {
        return (Expr) jjtGetChild(0);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=36a6c7059e4596742a6d4ff2c4d61869 (do not edit this line) */
