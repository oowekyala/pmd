/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Parameter of an {@link ASTInlineFunctionExpr InlineFunctionExpr}.
 * Wrapped in a {@link ASTParamList}.
 *
 * <p>Each parameter has a name and an optional type. If no type is specified,
 * its default type is item()*.
 *
 * <pre>
 *
 * Param ::= "$" {@link ASTName EQName} ( "as" {@link ASTSequenceType SequenceType} )?
 *
 * </pre>
 */
public final class ASTParam extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTParam() {
        super(XPathParserImplTreeConstants.JJTPARAM);
    }


    /**
     * Gets the node representing the name of the variable.
     */
    public ASTName getNameNode() {
        return (ASTName) getChild(0);
    }


    /**
     * Returns true if this parameter has no type annotation,
     * in which case {@code item()*} is assumed.
     */
    public boolean isDefaultType() {
        return getNumChildren() == 1;
    }

    // TODO synthesize default type node?


    /**
     * Returns the declared type of the parameter,
     * or empty if the default is used.
     */
    @Nullable
    public ASTSequenceType getDeclaredType() {
        return isDefaultType() ? null : (ASTSequenceType) getChild(1);
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
