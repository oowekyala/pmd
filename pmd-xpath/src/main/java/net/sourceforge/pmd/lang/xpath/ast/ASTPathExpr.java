/*
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
 * equivalent. This removal occurs at parse-time.
 *
 * <pre>
 *
 * PathExpr ::= "/" RelativePathExpr?
 *            | "//" RelativePathExpr
 *            | RelativePathExpr
 *
 * (: Not a node :)
 * RelativePathExpr ::= {@link StepExpr} (("/" || {@link StepExpr AbbrevDescendantOrSelfStep}) {@link StepExpr})*
 *
 * </pre>
 */
public final class ASTPathExpr extends AbstractXPathExpr implements Iterable<StepExpr>, Expr {

    private PathAnchor pathAnchor;


    /** Constructor for synthetic node. */
    public ASTPathExpr() {
        super(XPathParserImplTreeConstants.JJTPATHEXPR);
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


    /**
     * Sets the anchor of the path.
     */
    public void setPathAnchor(PathAnchor pathAnchor) {
        this.pathAnchor = pathAnchor;
    }


    /**
     * Gets the anchor of the first segment of the path.
     */
    public PathAnchor getPathAnchor() {
        return pathAnchor;
    }


    /**
     * Gets the first step.
     */
    public StepExpr getFirstStep() {
        return (StepExpr) getChild(0);
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
    public Iterator<StepExpr> iterator() {
        return children(StepExpr.class).iterator();
    }


    /**
     * Anchoring of a PathExpr. This only concerns the first step.
     */
    public enum PathAnchor {
        /** The PathExpr starts with "/". */
        ROOT("/"),
        /** The PathExpr starts with "//". */
        DESCENDANT_OR_ROOT("//"),
        /** The PathExpr starts with neither "/" nor "//". */
        RELATIVE("");


        private final String prefix;


        PathAnchor(String prefix) {
            this.prefix = prefix;
        }


        /**
         * Gets the string prefix corresponding to this anchor.
         */
        public String getPrefix() {
            return prefix;
        }
    }

}
