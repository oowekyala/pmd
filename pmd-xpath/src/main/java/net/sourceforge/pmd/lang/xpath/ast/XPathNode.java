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

    <T> T jjtAccept(XPathParserVisitor<T> visitor, T data);


    <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data);


    <T> T childrenAccept(XPathParserVisitor<T> visitor, T data);

}
