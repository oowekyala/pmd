/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vm.ast;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.Parser.ParserTask;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.util.document.TextDocument;

public final class ASTTemplate extends AbstractVmNode implements RootNode {

    private TextDocument textDocument;

    public ASTTemplate(int id) {
        super(id);
    }

    @Override
    public @NonNull TextDocument getTextDocument() {
        return textDocument;
    }

    ASTTemplate addTaskInfo(ParserTask task) {
        textDocument = task.getTextDocument();
        return this;
    }

    @Override
    public Object jjtAccept(VmParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
