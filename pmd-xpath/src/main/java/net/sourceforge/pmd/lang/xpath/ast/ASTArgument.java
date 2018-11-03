/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.Optional;


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
        super(null, XPathParserTreeConstants.JJTARGUMENT);
    }


    private boolean isPlaceholder;


    ASTArgument(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if this is a placeholder argument, i.e. "?".
     */
    public boolean isPlaceholder() {
        return isPlaceholder;
    }


    /**
     * Return the child, or an empty optional if this is a placeholder argument.
     */
    public Optional<ExprSingle> getExpression() {
        return isPlaceholder ? Optional.empty() : Optional.of((ExprSingle) jjtGetChild(0));
    }


    void setPlaceholder() {
        isPlaceholder = true;
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
/* JavaCC - OriginalChecksum=12d47f9d14e8d353f8b969e52983c8c1 (do not edit this line) */
