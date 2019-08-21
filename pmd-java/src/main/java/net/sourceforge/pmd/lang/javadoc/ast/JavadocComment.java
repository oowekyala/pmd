/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

/**
 * Root node of Javadoc ASTs.
 */
public class JavadocComment extends AbstractJavadocNode {

    public JavadocComment() {
        super(JavadocNodeId.ROOT);
    }

    @Override
    public String getXPathNodeName() {
        return null;
    }
}
