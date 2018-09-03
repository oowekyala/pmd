/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Reference to a named function. A named function is a function defined in the static
 * context for the expression. To uniquely identify a particular named function, both
 * its name as an expanded QName and its arity are required.
 *
 * <p>The arity is not denoted by a {@linkplain ASTNumericLiteral NumericLiteral} child.
 * Instead, it's available through {@link #getArity()}.
 *
 * <pre>
 *
 * NamedFunctionRef ::= {@linkplain ASTName EQName} "#" <INTEGER_LITERAL>
 *
 * </pre>
 */
public final class ASTNamedFunctionRef extends AbstractXPathNode implements FunctionItemExpr {

    private int arity = 0;


    ASTNamedFunctionRef(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Gets the arity of the referred function.
     */
    public int getArity() {
        return arity;
    }


    /**
     * Get the node representing the function name.
     */
    public ASTName getFunctionName() {
        return (ASTName) jjtGetChild(0);
    }


    void setArity(int arity) {
        this.arity = arity;
    }
}
/* JavaCC - OriginalChecksum=ec8948cb98fce8414cc0bdb9249c4c98 (do not edit this line) */
