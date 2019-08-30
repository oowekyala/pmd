/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.document.MutableDocument.SafeMutableDocument;
import net.sourceforge.pmd.document.patching.TextPatch;
import net.sourceforge.pmd.lang.ast.impl.JavaccToken;
import net.sourceforge.pmd.lang.rule.autofix.TreeEditSession;


class JavaEditSession implements TreeEditSession<JavaNode, JavaccToken> {

    private final SafeMutableDocument<TextPatch> document;
    private final TreeEditionHelper editor = new TreeEditionHelper(this);

    JavaEditSession(SafeMutableDocument<TextPatch> document) {
        this.document = document;
    }

    @Override
    public TextPatch commit() {
        return document.commit();
    }


    public SafeMutableDocument<TextPatch> getDocument() {
        return document;
    }

    @Override
    public TokenEditSession<JavaccToken> getTokenEditor() {
        return null;
    }

    @Override
    public void delete(JavaNode node) {
        if (node instanceof ASTCompilationUnit) {

        } else {
            editor.deleteInParent((AbstractJavaNode) node.jjtGetParent(), (AbstractJavaNode) node);
        }
    }

    @Override
    public void replace(JavaNode node, JavaNode replacement) {
        throw new UnsupportedOperationException("TODO");
    }
}
