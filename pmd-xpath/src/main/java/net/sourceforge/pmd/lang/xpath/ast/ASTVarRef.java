/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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


    /** Constructor for synthetic node. */
    public ASTVarRef() {
        super(null, XPathParserTreeConstants.JJTVARREF);
    }


    ASTVarRef(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the node representing the name of
     * the referenced variable.
     */
    public ASTName getVarNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    /**
     * Gets the name of the referenced variable.
     */
    public String getVarName() {
        return getVarNameNode().getLocalName();
    }


    /**
     * Returns the binding of the referenced variable
     * if it exists. Returns an empty optional if the
     * variable is free, i.e., the value for this variable
     * will be provided by the static or dynamic evaluation
     * context.
     *
     * <p>It's computed dynamically to make it resilient to AST
     * rewrites.
     */
    public Optional<ASTVarBinding> getBinding() {
        // these are excluded, since the scope of a variable binding should not include its initializer
        Set<ASTVarBinding> bindingParents = getParentStream().filter(x -> x instanceof ASTVarBinding)
                                                             .map(x -> (ASTVarBinding) x)
                                                             .collect(Collectors.toSet());

        return getParentStream().filter(x -> x instanceof BinderExpr)
                                .map(x -> (BinderExpr) x)
                                .flatMap(b -> b.getBindings().stream())
                                .filter(b -> !bindingParents.contains(b))
                                .filter(b -> b.getVarName().equals(getVarName()))
                                .findFirst();
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
/* JavaCC - OriginalChecksum=f684717c97ae752b7476d8ec9bfe515b (do not edit this line) */