/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

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


    ASTFunctionCall(XPathParser p, int id) {
        super(p, id);
    }


    public ASTName getFunctionName() {
        return (ASTName) jjtGetChild(0);
    }


    public ASTArgumentList getArguments() {
        return (ASTArgumentList) jjtGetChild(1);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=0aafee7da1d65c30d8e30966577689f4 (do not edit this line) */
