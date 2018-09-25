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
 */
public final class ASTInstanceofExpr extends AbstractXPathNode implements ExprSingle {


    ASTInstanceofExpr(XPathParser p, int id) {
        super(p, id);
    }


    public ExprSingle getTestedExpr() {
        return (ExprSingle) jjtGetChild(0);
    }


    public ASTSequenceType getTestedType() {
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
/* JavaCC - OriginalChecksum=f02361df2713ede12c9748ebb3f67690 (do not edit this line) */
