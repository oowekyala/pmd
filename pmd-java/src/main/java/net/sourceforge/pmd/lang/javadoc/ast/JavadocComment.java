/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.AbstractNode;

/**
 * @author Clément Fournier
 */
public class JavadocComment extends AbstractNode {

    public JavadocComment(int id) {
        super(id);
    }

    @Override
    public String getXPathNodeName() {
        return null;
    }
}
