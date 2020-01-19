/*
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
        super(XPathParserImplTreeConstants.JJTXPATHROOT);
    }


    /**
     * Returns the toplevel expression of the XPath tree.
     */
    public Expr getMainExpr() {
        return (Expr) getChild(0);
    }


    /**
     * Returns a set of references to free variables (variables
     * not bound by a {@link BinderExpr} within the expression itself).
     * Modifications on the returned set do not affect this node.
     */
    public Set<ASTVarRef> getFreeVarRefs() {
        FreeVarsResolver visitor = new FreeVarsResolver();
        this.jjtAccept(visitor, null);
        return visitor.getFreeVars();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Populates {@link ASTVarRef} with their corresponding {@link ASTVarBinding}.
     * This visitor is single use, and must be started on an XPathRoot node.
     */
    private static class FreeVarsResolver implements XPathSideEffectingVisitor<Void> {


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
        public void visit(ASTXPathRoot node, Void v) {
            bindings.clear();
            freeVars = new HashSet<>();
            XPathSideEffectingVisitor.super.visit(node, v);
        }


        private void addBindings(BinderExpr node) {
            for (ASTVarBinding binding : node.getBindings()) {
                // visit the initializer before putting the binding in scope
                binding.getInitializerExpr().jjtAccept(this, null);
                bindings.push(binding);
            }
            // now every binding is in scope

            node.getBodyExpr().jjtAccept(this, null);

            for (int i = 0; i < node.getBindings().count(); i++) {
                bindings.pop();
            }
        }


        @Override
        public void visit(ASTQuantifiedExpr node, Void v) {
            addBindings(node);
        }


        @Override
        public void visit(ASTForExpr node, Void v) {
            addBindings(node);
        }


        @Override
        public void visit(ASTLetExpr node, Void v) {
            addBindings(node);
        }


        @Override
        public void visit(ASTVarBinding node, Void v) {
            bindings.push(node);
            XPathSideEffectingVisitor.super.visit(node, v);
        }


        @Override
        public void visit(ASTVarRef node, Void v) {

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
