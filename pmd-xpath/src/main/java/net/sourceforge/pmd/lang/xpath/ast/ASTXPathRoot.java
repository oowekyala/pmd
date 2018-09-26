/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.RootNode;


/**
 * Root node of all XPath trees. Always has a unique child.
 */
public final class ASTXPathRoot extends AbstractXPathNode implements RootNode {

    /** Constructor for synthetic node. */
    ASTXPathRoot() {
        super(null, XPathParserTreeConstants.JJTXPATHROOT);
    }

    private Set<ASTVarRef> freeVars = new HashSet<>();


    ASTXPathRoot(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the main expr of the XPath tree.
     */
    public Expr getMainExpr() {
        return (Expr) jjtGetChild(0);
    }


    /** Add a reference to a free variable. */
    void addFreeVar(ASTVarRef ref) {
        freeVars.add(ref);
    }


    /**
     * Returns the set of references to free variables (variables not bound
     * by a {@link BinderExpr} within the expression itself).
     */
    public Set<ASTVarRef> getFreeVarRefs() {
        return Collections.unmodifiableSet(freeVars);
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
