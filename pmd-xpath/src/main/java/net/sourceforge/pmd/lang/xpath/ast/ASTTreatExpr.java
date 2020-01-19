/*
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
public final class ASTTreatExpr extends AbstractXPathExpr implements Expr {

    /** Constructor for synthetic node. */
    public ASTTreatExpr() {
        super(XPathParserImplTreeConstants.JJTTREATEXPR);
    }


    public Expr getCastedExpr() {
        return (Expr) getChild(0);
    }


    public ASTSequenceType getCastedType() {
        return (ASTSequenceType) getChild(1);
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
