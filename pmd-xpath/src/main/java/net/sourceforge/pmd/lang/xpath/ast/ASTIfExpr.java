/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Conditional expression.
 *
 * <pre>
 *
 * IfExpr ::= "if" "(" {@link Expr} ")" "then" {@link ExprSingle} "else" {@link ExprSingle}
 *
 * </pre>
 */
public final class ASTIfExpr extends AbstractXPathNode implements ExprSingle {


    ASTIfExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=bc4bcc1f2a1daa67c83fae268966da0d (do not edit this line) */
