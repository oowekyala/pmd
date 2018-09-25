/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;


/**
 * Populates {@link ASTVarRef} with their corresponding {@link ASTVarBinding}.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public class VarBindingResolver extends AbstractParameterlessSideEffectingVisitor {


    private Deque<ASTVarBinding> bindings = new ArrayDeque<>();


    @Override
    public void visit(ASTQuantifiedExpr node) {
        addBindings(node);
    }


    void addBindings(BinderExpr node) {
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
        super.visit(node);
    }


    @Override
    public void visit(ASTVarRef node) {

        Iterator<ASTVarBinding> iter = bindings.descendingIterator();

        while (iter.hasNext()) {
            ASTVarBinding b = iter.next();
            if (b.getVarName().equals(node.getVarName())) {
                node.setBinding(b);
                return;
            }
        }
    }
}
