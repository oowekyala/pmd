/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Interface for nodes that represent operators.
 */
public interface BinaryOperatorNode extends XPathNode {

    /**
     * Returns the image of the operator as it appeared in the source.
     */
    @Override
    String getImage();

    //    TODO
    //    Expr getLeftHandSide();
    //    Expr getRightHandSide();

}
