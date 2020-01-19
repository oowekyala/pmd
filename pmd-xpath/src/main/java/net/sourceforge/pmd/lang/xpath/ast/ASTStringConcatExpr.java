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

    /** Constructor for synthetic node. */
    public ASTStringConcatExpr() {
        super(XPathParserImplTreeConstants.JJTSTRINGCONCATEXPR);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=65fcb4661bdd318a3abb855db9e9bcd0 (do not edit this line) */
