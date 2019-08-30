/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.document.MutableDocument.SafeMutableDocument;
import net.sourceforge.pmd.document.patching.TextPatch;
import net.sourceforge.pmd.lang.rule.autofix.TreeEditSession;


class JavaEditSession implements TreeEditSession<JavaNode> {

    private final SafeMutableDocument<TextPatch> document;
    private final TreeEditionHelper editor = new TreeEditionHelper(this);

    JavaEditSession(SafeMutableDocument<TextPatch> document) {
        this.document = document;
    }

    @Override
    public TextPatch commit() {
        return document.commit();
    }


    SafeMutableDocument<TextPatch> getDocument() {
        return document;
    }

    @Override
    public void delete(JavaNode node) {
        if (node instanceof ASTCompilationUnit) {
            // TODO delete file
        } else {
            editor.deleteInParent((AbstractJavaNode) node.jjtGetParent(), (AbstractJavaNode) node);
        }
    }

}
