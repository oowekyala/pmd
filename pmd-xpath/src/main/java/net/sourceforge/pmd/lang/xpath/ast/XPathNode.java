/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;


/**
 * Root interface for all nodes of the XPath language.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface XPathNode extends Node {

    /**
     * Returns the last child of this node, or null if this node has no children.
     */
    @Nullable
    default XPathNode getLastChild() {
        return getNumChildren() > 0 ? getChild(getNumChildren() - 1) : null;
    }


    /**
     * Returns the last child of this node, or null if this node has no children.
     */
    @Nullable
    default XPathNode getFirstChild() {
        return getNumChildren() > 0 ? getChild(0) : null;
    }


    @Override
    XPathNode getParent();


    @Override
    XPathNode getChild(int index);


    @Override
    NodeStream<XPathNode> children();


    <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data);


    <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data);


}
