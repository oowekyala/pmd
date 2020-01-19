/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;

/**
 * Additive expression.
 * The XPath specification defines no associativity for these expressions.
 * For parsing simplicity, they're parsed left-associatively. Examples:
 * <ul>
 *     <li>{@code 1 + 2 - 3 * 4 + 5} is parsed as {@code (((1 + 2) - (3 * 4)) + 5)}</li>
 *     <li>{@code 1 + 2 + 5} is parsed as {@code (1 + 2) + 5}</li>
 * </ul>
 *
 * <pre>
 *
 * InfixExpr ::= {@link Expr} {@link XpBinaryOp} {@link Expr}
 *
 * </pre>
 */
public final class ASTInfixExpr extends AbstractXPathExpr implements Expr {

    private XpBinaryOp op;

    protected ASTInfixExpr(int id) {
        super(id);
    }


    void setOperator(JavaccToken token) {
        this.op = XpBinaryOp.fromTokenKind(token.kind);
    }


    /**
     * Returns the operator of this node, eg "+" or "-".
     */
    public XpBinaryOp getOperator() {
        return op;
    }


    @Override
    public NodeStream<Expr> children() {
        return (NodeStream<Expr>) super.children();
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
