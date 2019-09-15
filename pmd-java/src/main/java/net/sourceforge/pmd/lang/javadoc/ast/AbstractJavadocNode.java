/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.stream.Collectors;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;

class AbstractJavadocNode implements JavadocNode {

    private static final AbstractJavadocNode[] EMPTY_ARRAY = new AbstractJavadocNode[0];
    private final JavadocNodeId id;

    private JavadocToken firstToken;
    private JavadocToken lastToken;

    private AbstractJavadocNode[] children = EMPTY_ARRAY;
    private int childIndex;
    private AbstractJavadocNode parent;
    private Object userData;


    AbstractJavadocNode(JavadocNodeId id) {
        this.id = id;
    }


    @Override
    public void jjtAddChild(Node child, int index) {
        if (!(child instanceof AbstractJavadocNode)) {
            throw new IllegalArgumentException("Need a javadoc node, got " + child);
        }

        if (index >= children.length) {
            final AbstractJavadocNode[] newChildren = new AbstractJavadocNode[index + 1];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;
        }
        children[index] = (AbstractJavadocNode) child;
        child.jjtSetChildIndex(index);
        child.jjtSetParent(this);
    }

    @Override
    public void jjtSetChildIndex(int index) {
        this.childIndex = index;
    }

    @Override
    public int jjtGetChildIndex() {
        return childIndex;
    }

    @Override
    public JavadocNode jjtGetChild(int index) {
        return children[index];
    }

    @Override
    public int jjtGetNumChildren() {
        return children.length;
    }

    @Override
    public String getXPathNodeName() {
        return id.getXPathNodeName();
    }

    @Override
    public JavadocToken getFirstToken() {
        return firstToken;
    }

    @Override
    public JavadocToken getLastToken() {
        return lastToken;
    }

    void setFirstToken(JavadocToken token) {
        firstToken = token;
    }

    void setLastToken(JavadocToken token) {
        lastToken = token;
    }

    @Override
    public String getText() {
        return getFirstToken().rangeTo(getLastToken()).map(JavadocToken::getImage).collect(Collectors.joining());
    }

    @Override
    public int getBeginLine() {
        return firstToken.getBeginLine();
    }

    @Override
    public int getBeginColumn() {
        return firstToken.getBeginColumn();
    }

    @Override
    public int getEndLine() {
        return lastToken.getEndLine();
    }

    @Override
    public JavadocNode jjtGetParent() {
        return parent;
    }


    void appendChild(AbstractJavadocNode node) {
        jjtAddChild(node, jjtGetNumChildren());
    }

    @Override
    public void jjtOpen() {

    }

    @Override
    public void jjtClose() {
        if (lastToken == null && jjtGetNumChildren() > 0) {
            setLastToken(jjtGetChild(0).getLastToken());
        }
    }

    @Override
    public void jjtSetParent(Node parent) {
        if (!(parent instanceof AbstractJavadocNode)) {
            throw new IllegalArgumentException("Need a javadoc node, got " + parent);
        }
        this.parent = (AbstractJavadocNode) parent;
    }

    @Override
    public int getEndColumn() {
        return lastToken.getEndColumn();
    }


    // unsupported stuff, fuck those methods

    @Override
    public DataFlowNode getDataFlowNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDataFlowNode(DataFlowNode dataFlowNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getUserData() {
        return this.userData;
    }

    @Override
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChildAtIndex(int childIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getImage() {
        return null;
    }

    @Override
    public int jjtGetId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImage(String image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasImageEqualTo(String image) {
        return false;
    }


    @Override
    public String toString() {
        return getText();
    }
}
