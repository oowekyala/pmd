/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.impl.javacc.AbstractJjtreeNode;


/**
 * Base class for XPath nodes.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
abstract class AbstractXPathNode extends AbstractJjtreeNode<XPathNode> implements XPathNode {


    protected AbstractXPathNode(int id) {
        super(id);
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
        parent.children[this.getIndexInParent()] = null;
        parent.insertChild(node, this.getIndexInParent(), false);

        // remove reference to the parent to avoid memory leak
        this.jjtSetParent(null);
        this.jjtSetChildIndex(-1);

        AbstractXPathNode childImpl = (AbstractXPathNode) node;

        // Line numbers
        childImpl.beginLine = this.getBeginLine();
        childImpl.endLine = this.getEndLine();
        childImpl.beginColumn = this.getBeginColumn();
        childImpl.endColumn = this.getEndColumn();

        // and hope this node can be garbage collected
    }


    void insertSyntheticChild(XPathNode child, int index) {
        insertChild(child, index, true);
    }


    // insert a child at a given index
    private void insertChild(XPathNode child, int index, boolean setLineNums) {
        // Allow to insert a child at random index without overwriting
        // If the child is null, it is replaced. If it is not null, children are shifted
        if (children != null && index < children.length && children[index] != null) {
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
        return XPathParserImplTreeConstants.jjtNodeName[id];
    }
}
