/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Variable reference, one of the {@link PrimaryExpr primary expressions}.
 *
 * <pre>
 *
 * VarRef ::= "$" {@linkplain ASTName VarName}
 *
 * </pre>
 */
public final class ASTVarRef extends AbstractXPathExpr implements PrimaryExpr {


    /** Constructor for synthetic node. */
    public ASTVarRef() {
        super(XPathParserImplTreeConstants.JJTVARREF);
    }

    ASTVarRef(int id) {
        this();
    }


    /**
     * Gets the node representing the name of
     * the referenced variable.
     */
    public ASTName getVarNameNode() {
        return (ASTName) getChild(0);
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
    @Nullable
    public ASTVarBinding getBinding() {
        // these are excluded, since the scope of a variable binding should not include its initializer
        Set<ASTVarBinding> bindingParents = ancestors().filter(x -> x instanceof ASTVarBinding)
                                                       .map(x -> (ASTVarBinding) x)
                                                       .collect(Collectors.toSet());

        return ancestors(BinderExpr.class).flatMap(BinderExpr::getBindings)
                                          .filterNot(bindingParents::contains)
                                          .filterMatching(ASTVarBinding::getVarName, this.getVarName())
                                          .first();
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
