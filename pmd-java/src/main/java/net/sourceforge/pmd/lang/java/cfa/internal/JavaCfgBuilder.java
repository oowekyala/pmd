/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition.TRUE;

import java.util.List;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.cfa.internal.impl.CfgBuilder;
import net.sourceforge.pmd.lang.java.ast.ASTAssertStatement;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;
import net.sourceforge.pmd.lang.java.ast.ASTCatchClause;
import net.sourceforge.pmd.lang.java.ast.ASTContinueStatement;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTEmptyStatement;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate;
import net.sourceforge.pmd.lang.java.ast.ASTForeachStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLabeledStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLocalClassStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTResource;
import net.sourceforge.pmd.lang.java.ast.ASTResourceList;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpressionList;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchLabel;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSynchronizedStatement;
import net.sourceforge.pmd.lang.java.ast.ASTThrowStatement;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;

class JavaCfgBuilder extends CfgBuilder<JavaNode, JavaBuildingCtx> {

    public static final JavaCfgBuilder INSTANCE = new JavaCfgBuilder();
    public static final JavaCfgBuilder DFA_INSTANCE = new JavaCfgBuilder() {
        @Override
        protected JavaDfaBuildingCtx newStartCtx() {
            return JavaDfaBuildingCtx.startCtx();
        }
    };

    private JavaCfgBuilder() {
        // singleton
    }

    @Override
    protected JavaBuildingCtx newStartCtx() {
        return JavaBuildingCtx.startCtx();
    }

    // TODO this needs a visitor with signature <R,P> R visit(Node,P)
    static class MyVisitor extends JavaParserVisitorAdapter {

        public static final MyVisitor INSTANCE = new MyVisitor();

        @Override
        public Object visit(JavaNode node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            if (ctx instanceof JavaDfaBuildingCtx) {
                BasicBlock<JavaNode> block = ctx.createBlockImpl(singletonList(node));
                ctx.linkFromBefore(block, TRUE);
                return block;
            }
            return ctx.before;
        }

        @Override
        public Object visit(ASTBlock node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.statementSeqLink(node);
        }

        @Override
        public Object visit(ASTIfStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            EdgeCondition condition = ctx.getExprFactory().makeFromAst(node.getCondition());
            return ctx.ifThenElseLink(
                condition,
                node.getThenBranch(),
                node.getElseBranch()
            );
        }

        @Override
        public Object visit(ASTSwitchStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            ASTExpression expr = node.getTestedExpression();

            return ctx.fallThroughSwitchLink(
                node.findChildrenOfType(ASTSwitchLabel.class),
                // label -> conditions
                label -> NodeStream.fromIterable(label).toStream().map(it -> ctx.getExprFactory().equality(expr, it)).reduce(TRUE.negate(), EdgeCondition::or),
                // label -> statements
                label -> label.asStream().followingSiblings().takeWhile(it -> !(it instanceof ASTSwitchLabel)).filterIs(JavaNode.class).toList(),
                ASTSwitchLabel::isDefault
            );
        }

        @Override
        public Object visit(ASTExpressionStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return ctx.singleStatementLink(node);
        }

        @Override
        public Object visit(ASTResource node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return ctx.singleStatementLink(node);
        }

        @Override
        public Object visit(ASTLocalVariableDeclaration node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return ctx.singleStatementLink(node);
        }

        @Override
        public Object visit(ASTLocalClassStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.singleStatementLink(node);
        }

        @Override
        public Object visit(ASTStatementExpressionList node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.singleStatementLink(node);
        }

        @Override
        public Object visit(ASTSynchronizedStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            BasicBlock<JavaNode> syncStart = ctx.createBlock(emptyList());

            // there's no complementary edge
            ctx.linkFromBefore(syncStart, ctx.getExprFactory().monitorAcquired(node.getLockExpression()));

            return ctx.subCtx(syncStart).statementSeqLink(node.getBody());
        }

        @Override
        public Object visit(ASTEmptyStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            // ignore it
            return ctx.before;
        }

        @Override
        public Object visit(ASTLabeledStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.labeledStatementLink(node.getLabel(), node.getStatement());
        }

        @Override
        public Object visit(ASTBreakStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.breakLink(node, node.getImage());
        }

        @Override
        public Object visit(ASTContinueStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return ctx.continueLink(node, node.getImage());
        }

        @Override
        public Object visit(ASTReturnStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return ctx.returnLink(node);
        }

        @Override
        public Object visit(ASTThrowStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return ctx.throwLink(node);
        }

        @Override
        public Object visit(ASTAssertStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            EdgeCondition condition = ctx.getExprFactory().makeFromAst(node.getCondition());
            return ctx.assertLink(condition, node, node.getDetailMessageNode());
        }

        @Override
        public Object visit(ASTTryStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            ASTResourceList resources = node.getFirstChildOfType(ASTResourceList.class);
            ASTBlock body = node.getFirstChildOfType(ASTBlock.class);
            ASTFinallyClause finallyStmt = node.getFinally();
            ASTBlock finallyBlock = finallyStmt != null ? finallyStmt.getBlock() : null;

            Iterable<ASTCatchClause> catchStatements = node.getCatchClauses();

            return ctx.tryLink(body, NodeStream.fromIterable(resources), catchStatements, finallyBlock);
        }

        @Override
        public Object visit(ASTCatchClause node, Object data) {
            return node.getBlock().jjtAccept(this, data);
        }

        @Override
        public Object visit(ASTWhileStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;
            return makeLoop(
                ctx,
                ctx.getExprFactory().makeFromAst(node.getCondition()),
                //isE2Unconditional
                false,
                //initStmts
                emptyList(),
                //loopStartStmts
                emptyList(),
                //updateStmts
                emptyList(),
                node,
                node.getBody()
            );
        }

        @Override
        public Object visit(ASTDoStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            return makeLoop(
                ctx,
                ctx.getExprFactory().makeFromAst(node.getCondition()),
                //isE2Unconditional
                true,
                //initStmts
                emptyList(),
                //loopStartStmts
                emptyList(),
                //updateStmts
                emptyList(),
                node,
                node.getBody()
            );
        }

        @Override
        public Object visit(ASTForStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            ASTExpression condNode = node.getCondition();
            EdgeCondition condition = condNode == null ? TRUE : ctx.getExprFactory().makeFromAst(condNode);

            return makeLoop(
                ctx,
                condition,
                //isE2Unconditional
                false,
                //initStmts
                getInit(node),
                //loopStartStmts
                emptyList(),
                //updateStmts
                getUpdate(node),
                node,
                node.getBody()
            );
        }

        @Override
        public Object visit(ASTForeachStatement node, Object data) {
            JavaBuildingCtx ctx = (JavaBuildingCtx) data;

            ASTExpression iterableExpr = node.getIterableExpr();
            EdgeCondition condition = ctx.getExprFactory().iterableHasNext(iterableExpr);

            return makeLoop(
                ctx,
                condition,
                //isE2Unconditional
                false,
                //initStmts
                singletonList(iterableExpr),
                //loopStartStmts
                singletonList(node.getFirstChildOfType(ASTLocalVariableDeclaration.class)),
                //updateStmts
                emptyList(),
                node,
                node.getBody()
            );
        }

        private BasicBlock<JavaNode> makeLoop(
            JavaBuildingCtx ctx,
            EdgeCondition condition,
            boolean isDoLoop,
            List<? extends JavaNode> initStmts,
            List<? extends JavaNode> loopStartStmts,
            List<? extends JavaNode> updateStmts,
            ASTStatement loopNode,
            ASTStatement body
        ) {
            String loopName = loopNode.jjtGetParent() instanceof ASTLabeledStatement
                              ? ((ASTLabeledStatement) loopNode.jjtGetParent()).getLabel() : null;

            return ctx.makeLoop(
                condition, isDoLoop, initStmts, loopStartStmts, updateStmts, loopName, body
            );

        }

        // TODO move to ASTForStatement

        private List<ASTStatement> getUpdate(ASTForStatement node) {
            ASTForUpdate update = node.getFirstChildOfType(ASTForUpdate.class);
            return update == null ? emptyList() : update.findChildrenOfType(ASTStatement.class);
        }

        private List<ASTStatement> getInit(ASTForStatement forStatement) {
            ASTForInit init = forStatement.getFirstChildOfType(ASTForInit.class);
            return init == null ? emptyList() : init.findChildrenOfType(ASTStatement.class);
        }


    }
}
