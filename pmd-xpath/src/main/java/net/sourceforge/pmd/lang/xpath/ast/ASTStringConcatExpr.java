/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import javax.annotation.Nullable;

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

    /** Constructor for synthetic node. */
    public ASTStringConcatExpr() {
        super(null, XPathParserTreeConstants.JJTSTRINGCONCATEXPR);
    }


    ASTStringConcatExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    @Nullable
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=65fcb4661bdd318a3abb855db9e9bcd0 (do not edit this line) */
