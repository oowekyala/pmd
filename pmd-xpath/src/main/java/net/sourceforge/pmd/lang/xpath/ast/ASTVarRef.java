/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Variable reference, one of the {@link PrimaryExpr primary expressions}.
 *
 * <pre>
 *
 * VarRef ::= "$" {@linkplain ASTName VarName}
 *
 * </pre>
 */
public final class ASTVarRef extends AbstractXPathNode implements PrimaryExpr {

    // The binder for this variable
    // If null, then the variable is free in this expression
    private ASTVarBinding binding;


    ASTVarRef(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of
     * the referenced variable.
     */
    public ASTName getVariableName() {
        return (ASTName) jjtGetChild(0);
    }


    /**
     * Returns the binding of the referenced variable
     * if it exists. Returns null if the variable
     * {@linkplain #isFree() is free}.
     */
    public ASTVarBinding getBinding() {
        return binding;
    }


    /**
     * Returns true if the referenced variable is
     * not bound by a variable binding in this
     * expression. The value for this variable will be
     * provided by the static or dynamic evaluation
     * context.
     */
    public boolean isFree() {
        return binding == null;
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
/* JavaCC - OriginalChecksum=f684717c97ae752b7476d8ec9bfe515b (do not edit this line) */
