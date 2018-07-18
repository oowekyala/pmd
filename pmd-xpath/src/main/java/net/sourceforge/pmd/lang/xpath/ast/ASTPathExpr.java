/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


public final class ASTPathExpr extends AbstractXPathNode {


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


    public enum PathAnchor {
        ROOT,
        DESCENDANT_OR_ROOT,
        RELATIVE
    }

}
/* JavaCC - OriginalChecksum=0abb4b09a09e05581510519abd0e66cc (do not edit this line) */
