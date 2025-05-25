/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.ast.TextAvailableNode;

/** A node wrapping a single token. */
abstract class AbstractTokenNode extends AbstractJavadocNode implements TextAvailableNode {

    public AbstractTokenNode(JavadocNodeId id, JavadocToken token) {
        super(id);
        jjtSetFirstToken(token);
        jjtSetLastToken(token);
    }

    @Override
    public String getText() {
        return jjtGetFirstToken().getImage();
    }
}
