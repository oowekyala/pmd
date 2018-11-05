/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
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
        FreeVarsResolver visitor = new FreeVarsResolver();
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


    /**
     * Populates {@link ASTVarRef} with their corresponding {@link ASTVarBinding}.
     * This visitor is single use, and must be started on an XPathRoot node.
     *
     * @author Clément Fournier
     * @since 6.7.0
     */
    private static class FreeVarsResolver implements ParameterlessSideEffectingVisitor {


        private Deque<ASTVarBinding> bindings = new ArrayDeque<>();
        private Set<ASTVarRef> freeVars;


        /**
         * Returns the set of free variable references found after traversal.
         * Returns null if no tree has been traversed.
         */
        public Set<ASTVarRef> getFreeVars() {
            return freeVars;
        }


        @Override
        public void visit(ASTXPathRoot node) {
            bindings.clear();
            freeVars = new HashSet<>();
            visitChildren(node);
        }


        private void addBindings(BinderExpr node) {
            for (ASTVarBinding binding : node.getBindings()) {
                // visit the initializer before putting the binding in scope
                binding.getInitializerExpr().jjtAccept(this);
                bindings.push(binding);
            }
            // now every binding is in scope

            node.getBodyExpr().jjtAccept(this);

            for (int i = 0; i < node.getBindings().size(); i++) {
                bindings.pop();
            }
        }


        @Override
        public void visit(ASTQuantifiedExpr node) {
            addBindings(node);
        }


        @Override
        public void visit(ASTForExpr node) {
            addBindings(node);
        }


        @Override
        public void visit(ASTLetExpr node) {
            addBindings(node);
        }


        @Override
        public void visit(ASTVarBinding node) {
            bindings.push(node);
            visitChildren(node);
        }


        @Override
        public void visit(ASTVarRef node) {

            for (ASTVarBinding b : bindings) {
                if (b.getVarName().equals(node.getVarName())) {
                    return;
                }
            }

            // else the var is free
            freeVars.add(node);
        }
    }
}
/* JavaCC - OriginalChecksum=36a6c7059e4596742a6d4ff2c4d61869 (do not edit this line) */
