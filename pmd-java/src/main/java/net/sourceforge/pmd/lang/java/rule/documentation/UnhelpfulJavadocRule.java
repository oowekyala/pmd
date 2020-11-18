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

    private void checkMethodComment(ASTMethodOrConstructorDeclaration method, RuleContext ctx) {
        JdocComment javadoc = method.getJavadocTree();
        if (javadoc == null) {
            return;
        }

        ProblemCollector collector = new ProblemCollector();

        List<ASTVariableDeclaratorId> formals = method.getFormalParameters().toStream().map(ASTFormalParameter::getVarId).toList();

        for (JdocBlockTag blockTag : javadoc.descendants(JdocBlockTag.class)) {
            switch (blockTag.getTagName()) {
            case "@param":
                checkParamTag(collector, formals, blockTag);
                break;
            case "@throws":
            case "@exception":
                checkExceptionTag(collector, blockTag);
                break;
            }
        }

        collector.report(ctx);
    }

    private void checkParamTag(ProblemCollector collector, List<ASTVariableDeclaratorId> formals, JdocBlockTag paramTag) {
        String paramName = paramTag.getParamName();
        if (paramName == null) {
            return;
        }

        ASTVariableDeclaratorId formal = getFormal(formals, paramName);
        if (formal == null) {
            return;
        }

        JdocCommentData description = paramTag.firstChild(JdocCommentData.class);
        if (description == null) {
            collector.addProblem(paramTag, "@param", "Missing @param description");
        } else {
            JdocToken data = description.getSingleDataToken();
            if (data == null) {
                return;
            }

            JTypeMirror type = formal.getTypeMirror();
            String signature = TypePrettyPrint.prettyPrintWithSimpleNames(type);
            if (data.getImageCs().contentEquals(signature)
                || data.getImageCs().contentEquals(TypePrettyPrint.prettyPrintWithSimpleNames(type.getErasure()))) {
                collector.addProblem(paramTag, "@param", "Parameter description is just a type");
            }
        }
    }

    private void checkExceptionTag(ProblemCollector collector, JdocBlockTag throwsTag) {
        JdocCommentData description = throwsTag.firstChild(JdocCommentData.class);
        if (description == null) {
            collector.addProblem(throwsTag,
                                 throwsTag.getTagName(),
                                 "Missing " + throwsTag.getTagName() + " description");
        }
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

    /**
     * Aggregates violations, so as not to report 5 violations for a single
     * comment (usually, auto-generated comments have several problems).
     */
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
                    longMessage + " (there are other problems with ",
                    " in this comment)"
                );

                summary.forEach((type, count) -> {
                    if (type.equals(mainKey)) {
                        if (count > 1) {
                            joiner.add((count - 1) + " other " + type);
                        }
                    } else {
                        joiner.add(count + " " + type);
                    }
                });
                ctx.addViolationWithMessage(reportNode, joiner.toString());
            }
        }
    }
}
