/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.documentation;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavadocComment;
import net.sourceforge.pmd.lang.java.ast.JavadocCommentOwner;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;

/**
 * @author Clément Fournier
 */
public class JavadocExplorerRule extends AbstractJavaRule {

    private int numUncommented;
    private int total;

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEnumDeclaration node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTConstructorDeclaration node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEnumConstant node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        process(node, data);
        return super.visit(node, data);
    }

    private void process(JavadocCommentOwner commentOwner, Object data) {
        total++;
        JdocComment comment;
        try {
            final JavadocComment formal = commentOwner.getJavadocComment();
            comment = formal != null ? formal.getJdocTree() : null;
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        if (comment == null) {
            numUncommented++;
            return;
        }

        System.out.println(commentOwner.getReportLocation().startPosToStringWithFile() + " "
                               + comment.findDescendantsOfType(JavadocNode.class).size());
    }
}
