/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * String concatenation expression.
 *
 * <pre>
 *
 * StringConcatExpr ::= {@linkplain ASTRangeExpr RangeExpr} ( "||" {@linkplain ASTRangeExpr RangeExpr} )+
 *
 * </pre>
 */
public final class ASTStringConcatExpr extends AbstractXPathNode implements ExprSingle {


    ASTStringConcatExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=65fcb4661bdd318a3abb855db9e9bcd0 (do not edit this line) */
