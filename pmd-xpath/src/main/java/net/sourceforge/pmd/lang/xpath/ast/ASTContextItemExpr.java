/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Context item expression.
 *
 * <pre>
 *
 * ContextItemExpr ::= "."
 *
 * </pre>
 */
public final class ASTContextItemExpr extends AbstractXPathExpr implements PrimaryExpr {

    /** Constructor for synthetic node. */
    public ASTContextItemExpr() {
        super(XPathParserImplTreeConstants.JJTCONTEXTITEMEXPR);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


}
