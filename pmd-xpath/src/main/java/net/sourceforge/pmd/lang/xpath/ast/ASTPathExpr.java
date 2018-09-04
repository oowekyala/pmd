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
 * equivalent. This removal occurs at parse-time.
 *
 * <pre>
 *
 * PathExpr ::= "/" RelativePathExpr?
 *            | "//" RelativePathExpr
 *            | RelativePathExpr
 *
 * (: Not a node :)
 * RelativePathExpr ::= {@link StepExpr} (("/" || {@linkplain StepExpr AbbrevDescendantOrSelfStep}) {@link StepExpr})*
 *
 * </pre>
 */
public final class ASTPathExpr extends AbstractXPathNode implements Iterable<StepExpr>, ExprSingle {


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


    /**
     * Gets the anchor of the first segment of the path.
     */
    public PathAnchor getPathAnchor() {
        return pathAnchor;
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    @Override
    public Iterator<StepExpr> iterator() {
        return new NodeChildrenIterator<>(this, StepExpr.class);
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
/* JavaCC - OriginalChecksum=0abb4b09a09e05581510519abd0e66cc (do not edit this line) */
