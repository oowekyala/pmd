/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;


/**
 * Path expression. A path is a sequence of step expressions.
 *
 * <p>If a path expression is relative, and has a single step
 * that is a postfix expression, then the PathExpr/StepExpr context
 * around the PostfixExpr is removed, since it obscures the AST and is lexically
 * equivalent. This removal occurs at parse-time. TODO test
 *
 * <pre>
 *
 * PathExpr ::= "/" RelativePathExpr?
 *            | "//" RelativePathExpr
 *            | RelativePathExpr
 *
 * (: Not a node :)
 * RelativePathExpr ::= {@linkplain ASTStepExpr StepExpr} ({@linkplain ASTPathOperator PathOperator} {@linkplain ASTStepExpr StepExpr})*
 *
 * </pre>
 */
public final class ASTPathExpr extends AbstractXPathNode implements Iterable<ASTStepExpr> {

    // TODO we could remove the ASTPathOperator if we expand "//" to its full form

    private PathAnchor pathAnchor;


    ASTPathExpr(XPathParser p, int id) {
        super(p, id);
    }


    void setRootAnchor() {
        this.pathAnchor = PathAnchor.ROOT;
    }


    void setDescendantAnchor() {
        this.pathAnchor = PathAnchor.DESCENDANT_OR_ROOT;
    }


    void setRelativeAnchor() {
        this.pathAnchor = PathAnchor.RELATIVE;
    }


    public PathAnchor getPathAnchor() {
        return pathAnchor;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<ASTStepExpr> iterator() {
        return new NodeChildrenIterator<>(this, ASTStepExpr.class);
    }


    /**
     * Anchoring of a PathExpr. This only concerns the first step.
     */
    public enum PathAnchor {
        /** The PathExpr starts with "/". */
        ROOT,
        /** The PathExpr starts with "//". */
        DESCENDANT_OR_ROOT,
        /** The PathExpr starts with neither "/" nor "//". */
        RELATIVE
    }

}
/* JavaCC - OriginalChecksum=0abb4b09a09e05581510519abd0e66cc (do not edit this line) */
