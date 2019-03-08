/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.List;
import javax.annotation.Nullable;


/**
 * Postfix expression.
 *
 * <pre>
 *
 * PostfixExpr ::= {@link PrimaryExpr} ({@linkplain ASTPredicate Predicate} | {@linkplain ASTArgumentList ArgumentList})+
 *
 * </pre>
 */
public final class ASTPostfixExpr extends AbstractXPathNode implements ExprSingle, StepExpr {

    /** Constructor for synthetic node. */
    public ASTPostfixExpr() {
        super(null, XPathParserTreeConstants.JJTPOSTFIXEXPR);
    }


    ASTPostfixExpr(XPathParser p, int id) {
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


    @Override
    public List<ASTPredicate> getPredicates() {
        return findChildrenOfType(ASTPredicate.class);
    }
}
/* JavaCC - OriginalChecksum=1cca783774867afa020917f496b7be7a (do not edit this line) */
