/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.List;


/**
 * Map expression.
 *
 * <pre>
 *
 * MapExpr ::= {@link ASTPathExpr PathExpr} ( "!" {@link ASTPathExpr PathExpr} )+
 *
 * </pre>
 */
public final class ASTMapExpr extends AbstractXPathExpr implements Expr {

    /** Constructor for synthetic node. */
    public ASTMapExpr() {
        super(XPathParserImplTreeConstants.JJTMAPEXPR);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public List<Expr> getOperands() {
        return findChildrenOfType(Expr.class);
    }


}
