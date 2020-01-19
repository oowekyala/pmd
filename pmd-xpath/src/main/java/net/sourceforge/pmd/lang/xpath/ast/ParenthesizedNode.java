/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Represents a parenthesized node.
 *
 * @param <T> Type of the tree (type or expression)
 */
public interface ParenthesizedNode<T extends XPathNode> {

    /**
     * Returns the node wrapped in the parentheses.
     */
    T getWrappedNode();

}
