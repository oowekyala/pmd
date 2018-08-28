/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * A let-expression binds some names to expressions and allow these variables
 * to be referenced in its body.
 *
 * <pre>
 *
 * LetExpr ::= "let" {@linkplain ASTVarBindingList VarBindingList} "return" {@linkplain ASTExpr ExprSingle}
 *
 * </pre>
 */
public final class ASTLetExpr extends AbstractXPathNode {


    ASTLetExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=78269336e6bae8882c7fd308eae64438 (do not edit this line) */
