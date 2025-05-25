/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.stream.Collectors;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.TextAvailableNode;

abstract class AbstractJavadocNode extends AbstractNode implements JavadocNode {

    private final JavadocNodeId id;

    public AbstractJavadocNode(JavadocNodeId id) {
        super(id.ordinal());
        this.id = id;
    }

    @Override
    public JavadocNode jjtGetChild(int index) {
        return (JavadocNode) super.jjtGetChild(index);
    }

    @Override
    public String getXPathNodeName() {
        return id.getXPathNodeName();
    }

    @Override
    public JavadocToken getFirstToken() {
        return jjtGetFirstToken();
    }

    @Override
    public JavadocToken getLastToken() {
        return jjtGetLastToken();
    }

    @Override
    public JavadocToken jjtGetFirstToken() {
        return (JavadocToken) super.jjtGetFirstToken();
    }

    @Override
    public JavadocToken jjtGetLastToken() {
        return (JavadocToken) super.jjtGetLastToken();
    }

    @Override
    public void jjtSetFirstToken(GenericToken token) {
        super.firstToken = token;
    }

    @Override
    public void jjtSetLastToken(GenericToken token) {
        super.lastToken = token;
    }

    @Override
    public String getText() {
        return jjtGetFirstToken().rangeTo(jjtGetLastToken()).map(JavadocToken::getImage).collect(Collectors.joining());
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
        return (JavadocNode) super.jjtGetParent();
    }

    @Override
    public void jjtClose() {
        if (lastToken == null && jjtGetNumChildren() > 0) {
            jjtSetLastToken(jjtGetChild(0).getLastToken());
        }
    }

    @Override
    public int getEndColumn() {
        return lastToken.getEndColumn();
    }

    public JavadocNodeId getId() {
        return id;
    }

    @Override
    public String toString() {
        return getText();
    }
}
