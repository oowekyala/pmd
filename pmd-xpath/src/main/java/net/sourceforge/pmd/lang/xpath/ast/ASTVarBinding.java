/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Binding of a name to an expression, occurring in {@linkplain ASTVarBindingList VarBindingList}.
 * Bound variables may be referred to by {@linkplain ASTVarRef VarRef}, a
 * {@linkplain PrimaryExpr primary expression}.
 *
 * <p>Bindings have a different syntax depending on where they occur. In let-expressions, the symbol
 * {@code :=} is used, whereas in other expressions it's the keyword {@code in}.
 *
 * <pre>
 *
 * VarBinding ::= "$" {@linkplain ASTName VarName} ("in" | ":=") {@linkplain ASTExpr ExprSingle}
 *
 * </pre>
 */
public final class ASTVarBinding extends AbstractXPathNode {


    private String varName;


    ASTVarBinding(XPathParser p, int id) {
        super(p, id);
    }


    void setVarName(String varName) {
        this.varName = varName;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public String getVarName() {
        return varName;
    }
}
/* JavaCC - OriginalChecksum=0801906c16745525f42098e542a6ff4e (do not edit this line) */
