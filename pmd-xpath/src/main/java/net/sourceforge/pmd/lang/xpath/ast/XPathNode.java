/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import javax.annotation.Nullable;

import net.sourceforge.pmd.lang.ast.Node;


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
        return jjtGetNumChildren() > 0 ? (XPathNode) jjtGetChild(jjtGetNumChildren() - 1) : null;
    }


    /**
     * Returns the last child of this node, or null if this node has no children.
     */
    @Nullable
    default XPathNode getFirstChild() {
        return jjtGetNumChildren() > 0 ? (XPathNode) jjtGetChild(0) : null;
    }


    @Override
    XPathNode jjtGetParent();


    /**
     * Returns true if this node was not created by regular
     * parsing. Tokens may be missing.
     */
    boolean isSynthetic();


    @Nullable
    <T> T jjtAccept(XPathGenericVisitor<T> visitor, @Nullable T data);


    <T> void jjtAccept(SideEffectingVisitor<T> visitor, @Nullable T data);


    default void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Nullable
    <T> T childrenAccept(XPathGenericVisitor<T> visitor, @Nullable T data);


    <T> void childrenAccept(SideEffectingVisitor<T> visitor, @Nullable T data);


    void childrenAccept(ParameterlessSideEffectingVisitor visitor);

}
