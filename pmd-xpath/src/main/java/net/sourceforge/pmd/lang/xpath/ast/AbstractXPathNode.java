/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
public abstract class AbstractXPathNode extends AbstractNode implements XPathNode {

    private final XPathParser parser;


    protected AbstractXPathNode(XPathParser parser, int id) {
        super(id);
        this.parser = parser;
    }


    @Override
    public final <T> T childrenAccept(XPathParserVisitor<T> visitor, T data) {
        if (children != null) {
            for (Node child : children) {
                ((XPathNode) child).jjtAccept(visitor, data);
            }
        }
        return data;
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


    @Override
    public String getXPathNodeName() {
        return XPathParserTreeConstants.jjtNodeName[id];
    }
}
