/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;


import java.io.IOException;
import java.util.Collections;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.document.MutableDocument;
import net.sourceforge.pmd.document.ReplaceHandler;
import net.sourceforge.pmd.lang.java.ast.ASTWildcardType;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.autofix.Autofix;

public class UselessBoundRule extends AbstractJavaRule {


    @Override
    public Object visit(ASTWildcardType node, Object data) {
        if (node.hasUpperBound() && node.getTypeBoundNode().getImage().equals("Object")) {
            addViolation(data, node, Collections.singletonList(deleteAutofix(node)));
        }
        return null;
    }

    @NonNull
    private Autofix deleteAutofix(ASTWildcardType node) {
        return new Autofix() {
            @Override
            public <T> T apply(ReplaceHandler<T> handler) throws IOException {
                final MutableDocument<T> mut = node.getSourceDocument().newMutableDoc(handler);
                mut.delete(node.getTypeBoundNode().getRegion());
                return mut.commit();
            }
        };
    }
}
