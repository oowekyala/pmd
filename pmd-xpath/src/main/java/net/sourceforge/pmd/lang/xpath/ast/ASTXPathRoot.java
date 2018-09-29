/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Set;

import net.sourceforge.pmd.lang.ast.RootNode;


/**
 * Root node of all XPath trees. Always has a unique child.
 *
 * <pre>
 *
 * XPathRoot ::= {@link Expr}
 *
 * </pre>
 */
public final class ASTXPathRoot extends AbstractXPathNode implements RootNode {


    /** Constructor for synthetic node. */
    public ASTXPathRoot() {
        super(null, XPathParserTreeConstants.JJTXPATHROOT);
    }


    ASTXPathRoot(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the toplevel expression of the XPath tree.
     */
    public Expr getMainExpr() {
        return (Expr) jjtGetChild(0);
    }


    /**
     * Returns a set of references to free variables (variables
     * not bound by a {@link BinderExpr} within the expression itself).
     * Modifications on the returned set do not affect this node.
     */
    public Set<ASTVarRef> getFreeVarRefs() {
        VarBindingResolver visitor = new VarBindingResolver();
        this.jjtAccept(visitor);
        return visitor.getFreeVars();
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
/* JavaCC - OriginalChecksum=36a6c7059e4596742a6d4ff2c4d61869 (do not edit this line) */
