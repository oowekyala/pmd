/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;


/**
 * Base class for XPath nodes.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
abstract class AbstractXPathNode extends AbstractNode implements XPathNode {

    /** May be null if the node is synthetic. */
    protected final XPathParser parser;


    protected AbstractXPathNode(XPathParser parser, int id) {
        super(id);
        this.parser = parser;
    }


    XPathNode getLastChild() {
        return (XPathNode) jjtGetChild(jjtGetNumChildren() - 1);
    }


    @Override
    public boolean isSynthetic() {
        return parser == null;
    }


    @Override
    public final <T> T childrenAccept(XPathGenericVisitor<T> visitor, T data) {
        if (children != null) {
            for (Node child : children) {
                ((XPathNode) child).jjtAccept(visitor, data);
            }
        }
        return data;
    }


    @Override
    public final <T> void childrenAccept(SideEffectingVisitor<T> visitor, T data) {
        if (children != null) {
            for (Node child : children) {
                ((XPathNode) child).jjtAccept(visitor, data);
            }
        }
    }


    @Override
    public final void childrenAccept(ParameterlessSideEffectingVisitor visitor) {
        if (children != null) {
            for (Node child : children) {
                ((XPathNode) child).jjtAccept(visitor);
            }
        }
    }


    @Override
    public void jjtOpen() {
        if (beginLine == -1 && parser.token.next != null) {
            beginLine = parser.token.next.beginLine;
            beginColumn = parser.token.next.beginColumn;
        }
    }


    @Override
    public void jjtClose() {
        if (beginLine == -1 && (children == null || children.length == 0)) {
            beginColumn = parser.token.beginColumn;
        }
        if (beginLine == -1) {
            beginLine = parser.token.beginLine;
        }
        endLine = parser.token.endLine;
        endColumn = parser.token.endColumn;
    }


    /**
     * Replaces this node with the given node in the children of its parent.
     * This can lead to inconsistencies and runtime failures if the node is
     * not compatible with the grammar of its parent.
     *
     * <p>Also sets the parent of this node to null.
     *
     * @param node Node with which to replace this node
     */
    public void replaceWith(XPathNode node) {
        AbstractXPathNode parent = (AbstractXPathNode) jjtGetParent();
        if (parent == null) {
            throw new IllegalStateException();
        }
        parent.children[this.jjtGetChildIndex()] = null;
        parent.insertChild(node, this.jjtGetChildIndex(), false);

        // remove reference to the parent to avoid memory leak
        this.jjtSetParent(null);
        this.jjtSetChildIndex(-1);

        // and hope this node can be garbage collected
    }


    void insertSyntheticChild(XPathNode child, int index) {
        insertChild(child, index, true);
    }


    private void insertChild(XPathNode child, int index, boolean setLineNums) {
        // Allow to insert a child at random insert without overwriting
        if (children != null && index < children.length) {
            Node[] newChildren = new Node[children.length + 1];

            // toShift nodes are to the right of the insertion index
            int toShift = children.length - index;

            // copy the nodes before
            System.arraycopy(children, 0, newChildren, 0, index);

            // copy the nodes after
            System.arraycopy(children, index, newChildren, index + 1, toShift);
            children = newChildren;
        }
        super.jjtAddChild(child, index);
        child.jjtSetParent(this);

        if (setLineNums) {
            AbstractXPathNode childImpl = (AbstractXPathNode) child;

            // Line numbers
            childImpl.beginLine = this.getBeginLine();
            childImpl.endLine = this.getEndLine();
            childImpl.beginColumn = this.getBeginColumn();
            childImpl.endColumn = this.getEndColumn();
        }
    }


    /**
     * Dumps this tree to a parsable expression string.
     * Parsing the result should produce an equivalent tree.
     */
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder();
        this.jjtAccept(new ExpressionMakerVisitor(), sb);
        return sb.toString();
    }


    @Override
    public String getXPathNodeName() {
        return XPathParserTreeConstants.jjtNodeName[id];
    }
}
