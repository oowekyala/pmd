/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Instance of expression.
 *
 * <pre>
 *
 * InstanceofExpr ::=  {@linkplain ASTTreatExpr TreatExpr} "instance" "of" {@linkplain ASTSequenceType SequenceType}
 *
 * </pre>
 *
 */
public final class ASTInstanceofExpr extends AbstractXPathNode implements Expr {


    ASTInstanceofExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=f02361df2713ede12c9748ebb3f67690 (do not edit this line) */
