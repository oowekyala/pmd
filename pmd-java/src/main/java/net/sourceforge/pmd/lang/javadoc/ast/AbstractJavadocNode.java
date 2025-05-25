/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.EnumSet;
import java.util.stream.Collectors;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.DataMap;
import net.sourceforge.pmd.util.DataMap.DataKey;

class AbstractJavadocNode implements JavadocNode {

    private final DataMap<DataKey<?, ?>> userMap = DataMap.newDataMap();

    private static final AbstractJavadocNode[] EMPTY_ARRAY = new AbstractJavadocNode[0];
    private final JavadocNodeId id;

    private JdocToken firstToken;
    private JdocToken lastToken;

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

    JdocMalformed newError(EnumSet<JdocTokenType> expected, JdocToken actual) {
        JdocMalformed n = new JdocMalformed(expected, actual);
        appendChild(n);
        return n;
    }


    void appendChild(JavadocNode node) {
        jjtAddChild(node, jjtGetNumChildren());
    }


    @Override
    public AbstractJavadocNode getParent() {
        return parent;
    }

    @Override
    public int getIndexInParent() {
        return childIndex;
    }

    @Override
    public JavadocNode getChild(int index) {
        return children[index];
    }

    @Override
    public JavadocNode jjtGetChild(int index) {
        return getChild(index);
    }

    @Override
    public int getNumChildren() {
        return children.length;
    }

    @Override
    public final String getXPathNodeName() {
        return id.getXPathNodeName();
    }

    @Override
    public JdocToken getFirstToken() {
        return firstToken;
    }

    @Override
    public JdocToken getLastToken() {
        return lastToken;
    }

    void setFirstToken(JdocToken token) {
        firstToken = token;
    }

    void setLastToken(JdocToken token) {
        lastToken = token;
    }

    @Override
    public DataMap<DataKey<?, ?>> getUserMap() {
        return userMap;
    }

    @Override
    public String getText() {
        return getFirstToken().rangeTo(getLastToken()).map(JdocToken::getImage).collect(Collectors.joining());
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
    public String toString() {
        return getText();
    }
}
