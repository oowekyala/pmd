/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Root interface for all nodes of the XPath language.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface XPathNode extends Node {

    /**
     * Returns true if this node was not created by regular
     * parsing. Tokens may be missing.
     */
    boolean isSynthetic();


    <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data);


    <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data);


    default void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    <T> T childrenAccept(XPathGenericVisitor<T> visitor, T data);


    <T> void childrenAccept(SideEffectingVisitor<T> visitor, T data);


    void childrenAccept(ParameterlessSideEffectingVisitor visitor);

}
