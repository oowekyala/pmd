/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Treat as expression.
 *
 * <p>Like cast, the treat expression takes two operands: an expression and a SequenceType.
 * Unlike cast, however, treat does not change the dynamic type or value of its operand.
 * Instead, the purpose of treat is to ensure that an expression has an expected dynamic type
 * at evaluation time.
 *
 *
 * <pre>
 *
 * TreatExpr ::= {@linkplain ASTCastableExpr CastableExpr} "treat" "as" {@linkplain ASTSequenceType SequenceType}
 *
 * </pre>
 */
public final class ASTTreatExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTTreatExpr() {
        super(null, XPathParserTreeConstants.JJTTREATEXPR);
    }


    ASTTreatExpr(XPathParser p, int id) {
        super(p, id);
    }


    public ExprSingle getCastedExpr() {
        return (ExprSingle) jjtGetChild(0);
    }


    public ASTSequenceType getCastedType() {
        return (ASTSequenceType) jjtGetChild(1);
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
/* JavaCC - OriginalChecksum=76a4098f3b8f1d946194bf5eac353f0f (do not edit this line) */
