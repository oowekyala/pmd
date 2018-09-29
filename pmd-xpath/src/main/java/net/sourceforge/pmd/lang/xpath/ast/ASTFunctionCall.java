/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Objects;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Function call.
 *
 * <pre>
 *
 * FunctionCall ::= {@linkplain ASTName EQName} {@linkplain ASTArgumentList ArgumentList}
 *
 * </pre>
 */
public final class ASTFunctionCall extends AbstractXPathNode implements PrimaryExpr {

    /**
     * Constructor for synthetic node.
     */
    public ASTFunctionCall(ASTName functionName, ASTArgumentList arguments) {
        super(null, XPathParserTreeConstants.JJTFUNCTIONCALL);
        children = new Node[2];
        children[0] = Objects.requireNonNull(functionName);
        children[1] = Objects.requireNonNull(arguments);
    }


    ASTFunctionCall(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if this function call represents the true
     * boolean value.
     */
    public boolean isBooleanTrueLiteral() {
        return getFunctionNameNode().getLocalName().equals("true")
                && getFunctionNameNode().getNamespacePrefix().isEmpty()
                && getArguments().getArgumentNumber() == 0;
    }


    /**
     * Returns true if this function call represents the false
     * boolean value.
     */
    public boolean isBooleanFalseLiteral() {
        return getFunctionNameNode().getLocalName().equals("false")
                && getFunctionNameNode().getNamespacePrefix().isEmpty()
                && getArguments().getArgumentNumber() == 0;
    }


    /**
     * Returns true if this function call represents a boolean
     * literal (false() or true()).
     */
    public boolean isBooleanLiteral() {
        String localName = getFunctionNameNode().getLocalName();

        return getArguments().getArgumentNumber() == 0
                && getFunctionNameNode().getNamespacePrefix().isEmpty()
                && "true".equals(localName) || "false".equals(localName);
    }


    /**
     * Get the node representing the function name.
     */
    public ASTName getFunctionNameNode() {
        return (ASTName) jjtGetChild(0);
    }


    /**
     * Gets the (possibly empty) argument list of the function.
     */
    public ASTArgumentList getArguments() {
        return (ASTArgumentList) jjtGetChild(1);
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
/* JavaCC - OriginalChecksum=0aafee7da1d65c30d8e30966577689f4 (do not edit this line) */
