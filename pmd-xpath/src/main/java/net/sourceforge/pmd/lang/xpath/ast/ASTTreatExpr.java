/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Treat as expression.
 *
 * <pre>
 *
 * TreatExpr ::= {@linkplain ASTCastableExpr CastableExpr} "treat" "as" {@linkplain ASTSequenceType SequenceType}
 *
 * </pre>
 *
 */
public final class ASTTreatExpr extends AbstractXPathNode implements ExpressionNode {


    ASTTreatExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=76a4098f3b8f1d946194bf5eac353f0f (do not edit this line) */
