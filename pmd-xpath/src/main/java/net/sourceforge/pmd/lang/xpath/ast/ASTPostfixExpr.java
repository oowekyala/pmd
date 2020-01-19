/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.List;


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
        super(XPathParserImplTreeConstants.JJTPOSTFIXEXPR);
    }

    ASTPostfixExpr(int id) {
        this();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public List<ASTPredicate> getPredicates() {
        return findChildrenOfType(ASTPredicate.class);
    }
}
