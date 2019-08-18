/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Adapter for {@link SideEffectingVisitor}. See {@link JavaParserVisitorAdapter} for why this is needed.
 * Unless visit methods are overridden without calling {@code super.visit}, the visitor performs a full
 * depth-first tree walk.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public class SideEffectingVisitorAdapter<T> implements SideEffectingVisitor<T> {


    public void visit(ASTAnnotation node, T data) {
        visit((JavaNode) node, data);
    }

    @Override
    public void visit(ASTSingleMemberAnnotation node, T data) {
        visit((ASTAnnotation) node, data);
    }

    @Override
    public void visit(ASTNormalAnnotation node, T data) {
        visit((ASTAnnotation) node, data);
    }

    @Override
    public void visit(ASTMarkerAnnotation node, T data) {
        visit((ASTAnnotation) node, data);
    }

    // TODO delegation


    public void visit(ASTSwitchLabeledRule node, T data) {
        visit((JavaNode) node, data);
    }

    public void visit(ASTAnyTypeDeclaration node, T data) {
        visit((JavaNode) node, data);
    }


    public void visit(ASTExpression node, T data) {
        visit((JavaNode) node, data);
    }

    @Override
    public void visit(ASTInfixExpression node, T data) {
        visit((ASTExpression) node, data);
    }

    public void visit(ASTStatement node, T data) {
        visit((JavaNode) node, data);
    }

    @Override
    public void visit(ASTIfStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTAssertStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTExpressionStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTLabeledStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTLocalClassStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTEmptyStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTSwitchStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTWhileStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTDoStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTForStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTBreakStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTContinueStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTReturnStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTThrowStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTSynchronizedStatement node, T data) {
        visit((ASTStatement) node, data);
    }

    @Override
    public void visit(ASTTryStatement node, T data) {
        visit((ASTStatement) node, data);
    }
}
