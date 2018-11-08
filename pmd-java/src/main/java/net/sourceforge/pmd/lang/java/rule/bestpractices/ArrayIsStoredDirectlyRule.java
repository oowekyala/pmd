/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration.TypeKind;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEqualityExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

/**
 * If a method or constructor receives an array as an argument, the array should
 * be cloned instead of directly stored. This prevents future changes from the
 * user from affecting the original array.
 *
 * @since Created on Jan 17, 2005
 * @author mgriffa
 */
public class ArrayIsStoredDirectlyRule extends AbstractSunSecureRule {


    public ArrayIsStoredDirectlyRule() {
        addRuleChainVisit(ASTConstructorDeclaration.class);
        addRuleChainVisit(ASTMethodDeclaration.class);

    }

    @Override
    public Object visit(ASTConstructorDeclaration node, Object data) {
        List<ASTFormalParameter> arrs = getArraysParams(node.getParameters());
        // TODO check if one of these arrays is stored in a non local
        // variable

        if (node.getFirstParentOfAnyType(ASTAnyTypeDeclaration.class).getTypeKind().equals(TypeKind.ENUM)) {
            // #1413 we whitelist constructors of enums
            // Most of the time they're known at compile-time and so this isn't a violation.
            // Enum constants *may* use an array that comes from somewhere else, should we flag them?
            return data;
        }

        List<ASTBlockStatement> bs = node.findDescendantsOfType(ASTBlockStatement.class);
        checkAll(data, arrs, bs);
        return data;
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        final ASTFormalParameters params = node.getFirstDescendantOfType(ASTFormalParameters.class);
        List<ASTFormalParameter> arrs = getArraysParams(params);
        checkAll(data, arrs, node.findDescendantsOfType(ASTBlockStatement.class));
        return data;
    }

    private void checkAll(Object context, List<ASTFormalParameter> arrs, List<ASTBlockStatement> bs) {
        for (ASTFormalParameter element : arrs) {
            checkForDirectAssignment(context, element, bs);
        }
    }

    private String getExpressionVarName(Node e) {
        String assignedVar = getFirstNameImage(e);
        if (assignedVar == null) {
            ASTPrimarySuffix suffix = e.getFirstDescendantOfType(ASTPrimarySuffix.class);
            if (suffix != null) {
                assignedVar = suffix.getImage();
                ASTPrimaryPrefix prefix = e.getFirstDescendantOfType(ASTPrimaryPrefix.class);
                if (prefix != null) {
                    if (prefix.usesThisModifier()) {
                        assignedVar = "this." + assignedVar;
                    } else if (prefix.usesSuperModifier()) {
                        assignedVar = "super." + assignedVar;
                    }
                }
            }
        }
        return assignedVar;
    }

    /**
     * Checks if the variable designed in parameter is written to a field (not
     * local variable) in the statements.
     */
    private void checkForDirectAssignment(Object ctx, final ASTFormalParameter parameter,
                                          final List<ASTBlockStatement> bs) {
        final ASTVariableDeclaratorId vid = parameter.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
        final String varName = vid.getImage();
        for (ASTBlockStatement b : bs) {
            if (b.hasDescendantOfType(ASTAssignmentOperator.class)) {
                final ASTStatementExpression se = b.getFirstDescendantOfType(ASTStatementExpression.class);
                if (se == null || !(se.jjtGetChild(0) instanceof ASTPrimaryExpression)) {
                    continue;
                }
                String assignedVar = getExpressionVarName(se);
                if (assignedVar == null) {
                    continue;
                }

                ASTPrimaryExpression pe = (ASTPrimaryExpression) se.jjtGetChild(0);
                Node n = pe.getFirstParentOfType(ASTMethodDeclaration.class);
                if (n == null) {
                    n = pe.getFirstParentOfType(ASTConstructorDeclaration.class);
                    if (n == null) {
                        continue;
                    }
                }
                if (!isLocalVariable(assignedVar, n)) {
                    // TODO could this be more clumsy? We really
                    // need to build out the PMD internal framework more
                    // to support simply queries like "isAssignedTo()" or
                    // something
                    if (se.jjtGetNumChildren() < 3) {
                        continue;
                    }
                    ASTExpression e = (ASTExpression) se.jjtGetChild(2);
                    if (e.hasDescendantOfType(ASTEqualityExpression.class)) {
                        continue;
                    }
                    String val = getExpressionVarName(e);
                    if (val == null) {
                        continue;
                    }
                    ASTPrimarySuffix foo = e.getFirstDescendantOfType(ASTPrimarySuffix.class);
                    if (foo != null && foo.isArrayDereference()) {
                        continue;
                    }

                    if (val.equals(varName)) {
                        Node md = parameter.getFirstParentOfType(ASTMethodDeclaration.class);
                        if (md == null) {
                            md = pe.getFirstParentOfType(ASTConstructorDeclaration.class);
                        }
                        if (!isLocalVariable(varName, md)) {
                            addViolation(ctx, parameter, varName);
                        }
                    }
                }
            }
        }
    }

    private List<ASTFormalParameter> getArraysParams(ASTFormalParameters params) {
        List<ASTFormalParameter> arrayParams = new ArrayList<>(1);
        for (ASTFormalParameter param : params) {
            if (param.isArray()) {
                arrayParams.add(param);
            }
        }
        return arrayParams;
    }

}
