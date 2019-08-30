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
        ((AbstractJavaNode) node).deleteMe(this);
    }

    @Override
    public void replace(JavaNode node, JavaNode replacement) {
        ((AbstractJavaNode) node).replaceBy(replacement, this);
    }
}
