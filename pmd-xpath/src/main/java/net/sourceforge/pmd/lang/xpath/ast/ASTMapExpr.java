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
 * MapExpr ::= {@linkplain ASTPathExpr PathExpr} ( "!" {@linkplain ASTPathExpr PathExpr} )+
 *
 * </pre>
 */
public final class ASTMapExpr extends AbstractXPathNode implements ExprSingle {

    /** Constructor for synthetic node. */
    public ASTMapExpr() {
        super(XPathParserImplTreeConstants.JJTMAPEXPR);
    }

    ASTMapExpr(int id) {
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


    public List<ExprSingle> getOperands() {
        return findChildrenOfType(ExprSingle.class);
    }


}
