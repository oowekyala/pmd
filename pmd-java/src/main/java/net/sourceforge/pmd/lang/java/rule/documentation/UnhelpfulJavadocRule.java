/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.documentation;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
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
 * @author Clément Fournier
 */
public class UnhelpfulJavadocRule extends AbstractJavaRulechainRule {

    public UnhelpfulJavadocRule() {
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
                collector.addProblem(param, "@param", "Missing @param description");
            } else {
                JdocToken data = description.getSingleDataToken();
                if (data == null) {
                    continue;
                }

                JTypeMirror type = formal.getTypeMirror();
                String signature = TypePrettyPrint.prettyPrintWithSimpleNames(type);
                if (data.getImageCs().contentEquals(signature)
                    || data.getImageCs().contentEquals(TypePrettyPrint.prettyPrintWithSimpleNames(type.getErasure()))) {
                    collector.addProblem(param, "@param", "Parameter description is just a type");
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
        String mainKey;


        void addProblem(JavadocNode node, String key, String longMessage) {
            size++;
            if (size == 1) {
                reportNode = node;
                this.longMessage = longMessage;
                mainKey = key;
                shortProblems = new ArrayList<>();
            } else if (size == 2) {
                // prefer reporting on the topmost
                if (node.compareLocation(reportNode) <= 0) {
                    reportNode = node;
                    this.longMessage = longMessage;
                    mainKey = key;
                }
            }
            shortProblems.add(key);
        }

        void report(RuleContext ctx) {
            if (size == 0) {
                return;
            }
            if (size == 1) {
                ctx.addViolationWithMessage(reportNode, longMessage);
            } else {

                Map<String, Long> summary = shortProblems.stream().collect(Collectors.groupingBy(it -> it, Collectors.counting()));

                StringJoiner joiner = new StringJoiner(
                    ", ",
                    longMessage + " (there are problems with ",
                    " in this comment)"
                );

                summary.forEach((type, count) -> {
                    if (type.equals(mainKey)) {
                        joiner.add((count - 1) + " other " + type);
                    } else {
                        joiner.add(count + " " + type);
                    }
                });
                ctx.addViolationWithMessage(reportNode, joiner.toString());
            }
        }
    }
}
