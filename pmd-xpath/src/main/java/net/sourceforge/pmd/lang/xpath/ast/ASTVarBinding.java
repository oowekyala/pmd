/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Binding of a name to an expression, occurring in a {@linkplain BinderExpr binder expressions}.
 * Bound variables may be referred to by {@linkplain ASTVarRef VarRef}, which is a {@linkplain PrimaryExpr primary expression}.
 *
 * <p>Bindings have a different syntax depending on where they occur. In let-expressions, the symbol
 * {@code :=} is used, whereas in other expressions it's the keyword {@code in}.
 *
 * <pre>
 *
 * VarBinding ::= "$" {@linkplain ASTName VarName} ("in" | ":=") {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTVarBinding extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    ASTVarBinding() {
        super(null, XPathParserTreeConstants.JJTVARBINDING);
    }


    ASTVarBinding(XPathParser p, int id) {
        super(p, id);
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
     * Returns true if the binder is written ":=", which is the
     * style used in let expressions. Other expressions use the
     * token "in".
     */
    public boolean isLetStyle() {
        return jjtGetParent() instanceof ASTLetExpr;
    }


    /**
     * Returns the expression initializing the variable.
     */
    public ExprSingle getInitializerExpr() {
        return (ExprSingle) jjtGetChild(1);
    }


    /**
     * Returns the node representing the name of the variable.
     */
    public ASTName getVarNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    /**
     * Returns the name of the variable.
     */
    public String getVarName() {
        return getVarNameNode().getLocalName();
    }
}
