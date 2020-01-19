/*
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
 * NamedFunctionRef ::= {@linkplain ASTName EQName} "#" &lt;INTEGER_LITERAL&gt;
 *
 * </pre>
 */
public final class ASTNamedFunctionRef extends AbstractXPathExpr implements FunctionItemExpr {

    /** Constructor for synthetic node. */
    public ASTNamedFunctionRef() {
        super(XPathParserImplTreeConstants.JJTNAMEDFUNCTIONREF);
    }

    ASTNamedFunctionRef(int id) {
        this();
    }

    private int arity = 0;


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
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
        return (ASTName) getChild(0);
    }


    void setArity(int arity) {
        this.arity = arity;
    }
}
