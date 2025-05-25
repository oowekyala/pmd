/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.AbstractNode;

/**
 * @author Clément Fournier
 */
abstract class AbstractJavadocNode extends AbstractNode {

    public AbstractJavadocNode(int id) {
        super(id);
    }
}
