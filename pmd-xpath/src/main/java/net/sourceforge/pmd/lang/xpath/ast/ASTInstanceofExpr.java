/*
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
 */
public final class ASTInstanceofExpr extends AbstractXPathNode implements ExprSingle {


    /** Constructor for synthetic node. */
    public ASTInstanceofExpr() {
        super(XPathParserImplTreeConstants.JJTINSTANCEOFEXPR);
    }

    ASTInstanceofExpr(int id) {
        this();
    }


    public ExprSingle getTestedExpr() {
        return (ExprSingle) getChild(0);
    }


    public ASTSequenceType getTestedType() {
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
