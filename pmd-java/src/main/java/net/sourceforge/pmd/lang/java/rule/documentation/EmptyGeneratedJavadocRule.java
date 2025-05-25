/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.documentation;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.JavadocCommentOwner;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypePrettyPrint;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocComment;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JdocBlockTag;
import net.sourceforge.pmd.lang.javadoc.ast.JdocToken;


/**
 * @author Brian Remedios
 */
public class EmptyGeneratedJavadocRule extends AbstractJavaRulechainRule {

    public EmptyGeneratedJavadocRule() {
        super(JavadocCommentOwner.class);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        //        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTEnumDeclaration node, Object data) {
        //        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        checkMethodComment(node, (RuleContext) data);
        return data;
    }

    @Override
    public Object visit(ASTConstructorDeclaration node, Object data) {
        checkMethodComment(node, (RuleContext) data);
        return data;
    }

    // todo report single violation for each comment
    private void checkMethodComment(ASTMethodOrConstructorDeclaration method, RuleContext ctx) {
        JdocComment javadoc = method.getJavadocTree();
        if (javadoc == null) {
            return;
        }

        ProblemCollector collector = new ProblemCollector();

        List<ASTVariableDeclaratorId> formals = method.getFormalParameters().toStream().map(ASTFormalParameter::getVarId).toList();

        for (JdocBlockTag param : javadoc.descendants(JdocBlockTag.class)
                                         .filter(it -> it.isA("@param"))) {

            String paramName = param.getParamName();
            if (paramName == null) {
                continue;
            }

            ASTVariableDeclaratorId formal = getFormal(formals, paramName);
            if (formal == null) {
                continue;
            }

            JdocCommentData description = param.firstChild(JdocCommentData.class);
            if (description == null) {
                collector.addProblem(param, "This @param tag has no description", "@param");
            } else {
                JdocToken data = description.getSingleDataToken();
                if (data == null) {
                    continue;
                }

                JTypeMirror type = formal.getTypeMirror();
                String signature = TypePrettyPrint.prettyPrintWithSimpleNames(type);
                if (data.getImageCs().contentEquals(signature)
                    || data.getImageCs().contentEquals(TypePrettyPrint.prettyPrintWithSimpleNames(type.getErasure()))) {
                    collector.addProblem(param, "Type signatures should not be used as description", "@param");
                }
            }
        }

        collector.report(ctx);
    }

    private ASTVariableDeclaratorId getFormal(List<ASTVariableDeclaratorId> formals, String paramName) {
        for (ASTVariableDeclaratorId fi : formals) {
            if (fi.getName().equals(paramName)) {
                return fi;
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTEnumConstant node, Object data) {
        //        process(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        //        process(node, data);
        return super.visit(node, data);
    }

    static final class ProblemCollector {

        private int size;
        List<String> shortProblems;
        JavadocNode reportNode;
        String longMessage;


        void addProblem(JavadocNode node, String longMessage, String shortMessage) {
            size++;
            if (size == 1) {
                reportNode = node;
                this.longMessage = longMessage;
                shortProblems = new ArrayList<>();
            } else if (size == 2) {
                // second message, fallback to aggregate report
                reportNode = node.getRoot();
                this.longMessage = null;
            }
            shortProblems.add(shortMessage);
        }

        void report(RuleContext ctx) {
            if (size == 0) {
                return;
            }
            if (size == 1) {
                ctx.addViolationWithMessage(reportNode, longMessage);
            } else {
                StringBuilder stringBuilder = new StringBuilder("This comment contains low quality content (");
                Map<String, Long> summary = shortProblems.stream().collect(Collectors.groupingBy(it -> it, Collectors.counting()));
                summary.forEach((type, count) -> stringBuilder.append(count).append(' ').append(type));
                stringBuilder.append(')');
                ctx.addViolationWithMessage(reportNode, stringBuilder.toString());
            }
        }
    }
}
