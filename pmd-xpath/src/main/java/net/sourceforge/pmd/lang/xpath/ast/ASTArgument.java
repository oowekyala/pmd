/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


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


    void setPlaceholder() {
        isPlaceholder = true;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=12d47f9d14e8d353f8b969e52983c8c1 (do not edit this line) */
