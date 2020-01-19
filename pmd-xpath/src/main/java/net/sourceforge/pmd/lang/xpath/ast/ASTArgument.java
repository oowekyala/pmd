/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Argument of a {@linkplain ASTFunctionCall}.
 *
 * <pre>
 *
 * Argument ::= {@link ExprSingle} | "?"
 *
 * </pre>
 */
public final class ASTArgument extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTArgument() {
        super(XPathParserImplTreeConstants.JJTARGUMENT);
    }


    private boolean isPlaceholder;


    /**
     * Returns true if this is a placeholder argument, i.e. "?".
     */
    public boolean isPlaceholder() {
        return isPlaceholder;
    }


    /**
     * Return the child, or an empty optional if this is a placeholder argument.
     */
    @Nullable
    public ExprSingle getExpression() {
        return isPlaceholder ? null : (ExprSingle) jjtGetChild(0);
    }


    void setPlaceholder() {
        isPlaceholder = true;
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
/* JavaCC - OriginalChecksum=12d47f9d14e8d353f8b969e52983c8c1 (do not edit this line) */
