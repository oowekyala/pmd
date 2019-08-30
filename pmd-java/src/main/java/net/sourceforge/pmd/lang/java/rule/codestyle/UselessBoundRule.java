/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;


import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.impl.JavaccToken;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeParameter;
import net.sourceforge.pmd.lang.java.ast.ASTWildcardType;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.autofix.Autofix;

public class UselessBoundRule extends AbstractJavaRule {


    @Override
    public Object visit(ASTWildcardType node, Object data) {
        if (node.hasUpperBound() && node.getTypeBoundNode().hasImageEqualTo("Object")) {
            addViolation(data, node, onlyDelete((RuleContext) data, node.getTypeBoundNode()));
        }
        return null;
    }


    @Override
    public Object visit(ASTTypeParameter node, Object data) {
        if (node.hasTypeBound() && node.getTypeBoundNode().hasImageEqualTo("Object")) {
            addViolation(data, node, onlyDelete((RuleContext) data, node.getTypeBoundNode()));
        }
        return null;
    }

    @NonNull
    private List<Autofix<JavaNode, JavaccToken>> onlyDelete(RuleContext data, ASTReferenceType typeBoundNode) {
        return Collections.singletonList(deleteAutofix(typeBoundNode, data.getLanguageVersion()));
    }

    @NonNull
    private static Autofix<JavaNode, JavaccToken> deleteAutofix(JavaNode node, LanguageVersion version) {
        return Autofix.from("Delete node", version, session -> session.delete(node));
    }
}
