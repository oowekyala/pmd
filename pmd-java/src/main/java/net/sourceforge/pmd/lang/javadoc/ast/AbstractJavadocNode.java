/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import java.util.EnumSet;

import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.document.TextRegion;
import net.sourceforge.pmd.util.StringUtil;

class AbstractJavadocNode extends AbstractNode<AbstractJavadocNode, JavadocNode> implements JavadocNode {

    private final JavadocNodeId id;

    private JdocToken firstToken;
    private JdocToken lastToken;


    AbstractJavadocNode(JavadocNodeId id) {
        this.id = id;
    }


    @Override
    protected void addChild(AbstractJavadocNode child, int index) {
        super.addChild(child, index);
    }

    JdocMalformed newError(EnumSet<JdocTokenType> expected, JdocToken actual) {
        JdocMalformed n = new JdocMalformed(expected, actual);
        appendChild(n);
        return n;
    }


    void appendChild(JavadocNode node) {
        addChild((AbstractJavadocNode) node, getNumChildren());
    }

    void closeNode() {

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
    public Chars getText() {
        return getTextDocument().sliceTranslatedText(getTextRegion());
    }

    @Override
    public TextRegion getTextRegion() {
        return TextRegion.union(getFirstToken().getRegion(), getLastToken().getRegion());
    }

    @Override
    public FileLocation getReportLocation() {
        return getFirstToken().getReportLocation();
    }

    /**
     * This toString implementation is only meant for debugging purposes.
     */
    @Override
    public String toString() {
        FileLocation loc = getReportLocation();
        return "!debug only! [" + getXPathNodeName() + ":" + loc.getStartPos().toDisplayStringWithColon() + "]"
            + StringUtil.elide(getText().toString(), 150, "(truncated)");
    }
}
