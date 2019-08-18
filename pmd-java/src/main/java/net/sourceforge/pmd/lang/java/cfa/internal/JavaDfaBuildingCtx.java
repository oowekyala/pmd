/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.TreeWalkUtils;

// package
class JavaDfaBuildingCtx extends JavaBuildingCtx {

    private JavaDfaBuildingCtx(Map<BlockKind, BasicBlock<JavaNode>> specialNodes, List<BasicBlock<JavaNode>> otherErrorHandlers, BasicBlock<JavaNode> before) {
        super(specialNodes, otherErrorHandlers, before);
    }

    private JavaDfaBuildingCtx() {
        super();
    }


    @Override
    protected JavaDfaBuildingCtx makeSubCtx(Map<BlockKind, BasicBlock<JavaNode>> specialNodes, List<BasicBlock<JavaNode>> otherErrorHandlers, BasicBlock<JavaNode> before) {
        return new JavaDfaBuildingCtx(specialNodes, otherErrorHandlers, before);
    }

    @Override
    public void link(BasicBlock<JavaNode> from, EdgeTarget<JavaNode> to) {
        super.link(from, to);
    }


    @Override
    public EdgeTarget<JavaNode> createTarget(BasicBlock<JavaNode> block, EdgeCondition condition) {
        return super.createTarget(block, EdgeCondition.TRUE);
    }

    @Override
    protected BasicBlock<JavaNode> visitTopDown(JavaNode node) {
        if (node instanceof ASTBlock) {
            return super.visitTopDown(node);
        }

        BasicBlock<JavaNode> sup = super.visitTopDown(node);
        if (sup != null) {
            return sup;
        } else {
            return statementSeqLink(getSubExpressions(node));
        }
    }

    @Override
    public BasicBlock<JavaNode> createBlock(List<? extends JavaNode> statements) {
        return statementSeqLink(NodeStream.fromIterable(statements).flatMap(this::getSubExpressions));
    }

    @Override
    public BasicBlock<JavaNode> singleStatementLink(JavaNode stmt) {
        // the method links it correctly to the before
        return createBlock(Collections.singletonList(stmt));
    }

    @Override
    public BasicBlock<JavaNode> createFakeBlock() {
        return createBlockImpl(Collections.emptyList());
    }

    private NodeStream<JavaNode> getSubExpressions(JavaNode node) {
        List<JavaNode> list = new ArrayList<>();
        TreeWalkUtils.postOrderWalk(node, n -> {
            if (n instanceof ASTExpression
                || n instanceof ASTVariableDeclarator) {
                list.add(n);
            }
        });
        return NodeStream.fromIterable(list);
    }

    static JavaDfaBuildingCtx startCtx() {
        return new JavaDfaBuildingCtx();
    }

}
