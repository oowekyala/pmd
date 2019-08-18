/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal;

import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.exprs.SymbolicExprFactory;
import net.sourceforge.pmd.lang.cfa.internal.impl.CommonBlockBuildingCtx;
import net.sourceforge.pmd.lang.java.ast.ASTCatchClause;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.cfa.internal.JavaCfgBuilder.MyVisitor;

// package
class JavaBuildingCtx extends CommonBlockBuildingCtx<JavaNode, JavaBuildingCtx> {

    JavaBuildingCtx(Map<BlockKind, BasicBlock<JavaNode>> specialNodes, List<BasicBlock<JavaNode>> otherErrorHandlers, BasicBlock<JavaNode> before) {
        super(specialNodes, otherErrorHandlers, before);
    }

    JavaBuildingCtx() {
        super();
    }

    @Override
    protected JavaNode getConditionNodeOfCatch(JavaNode catchStmt) {
        if (catchStmt instanceof ASTCatchClause) {
            return catchStmt.getFirstChild();
        } else {
            throw new IllegalArgumentException("Expected an ASTCatchStatement, got " + catchStmt);
        }
    }

    @Override
    public SymbolicExprFactory<JavaNode> getExprFactory() {
        return JavaExprFactory.INSTANCE;
    }

    @Override
    protected BasicBlock<JavaNode> visitTopDown(JavaNode node) {
        return (BasicBlock<JavaNode>) node.jjtAccept(MyVisitor.INSTANCE, this);
    }

    @Override
    protected JavaBuildingCtx makeSubCtx(Map<BlockKind, BasicBlock<JavaNode>> specialNodes, List<BasicBlock<JavaNode>> otherErrorHandlers, BasicBlock<JavaNode> before) {
        return new JavaBuildingCtx(specialNodes, otherErrorHandlers, before);
    }

    BasicBlock<JavaNode> createBlockImpl(List<? extends JavaNode> nodes) {
        return super.createBlock(nodes);
    }

    static JavaBuildingCtx startCtx() {
        return new JavaBuildingCtx();
    }
}
