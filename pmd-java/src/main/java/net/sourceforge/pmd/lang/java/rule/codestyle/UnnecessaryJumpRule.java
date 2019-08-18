/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTContinueStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.cfa.internal.JavaCfa;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Unconditional jumps are of the form:
 *
 * <pre>
 *  A +---(true)----> B
 *    +---(false)---> C
 * </pre>
 *
 * <p>The jump instruction causes control to jump to B, where without
 * the jump it would have flown to C.
 *
 * <p>The jump is unnecessary if there exists a single path from C to B,
 * only conditioned by {@link EdgeCondition#TRUE}, and containing no
 * statements to execute. That means, that without the jump, the next
 * statement to be executed would be the same as with the jump.
 */
public class UnnecessaryJumpRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTConstructorDeclaration node, Object data) {
        check(node.getBody(), data);
        return data;
    }


    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (!node.isAbstract()) {
            check(node.getBlock(), data);
        }
        return data;
    }

    /*
        TODO this has the problem, that CFGs are entirely explored, even
          though maybe there is no return/continue/break statement.
     */


    private boolean isJump(JavaNode node) {
        return node instanceof ASTReturnStatement || node instanceof ASTContinueStatement
            || node instanceof ASTBreakStatement;
    }

    private void check(ASTBlock block, Object ruleCtx) {
        FlowGraph<JavaNode> cfg = JavaCfa.INSTANCE.getBuilder().buildCfg(block);
        try {
            JavaCfa.INSTANCE.getPrinters().latex().renderToFile(cfg, Paths.get(System.getProperty("user.home")).resolve(".pmd").resolve("cfg").resolve("cfg.tex"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        cfg.streamBlocks()
           .filter(it -> it.getStatements().size() == 1 && isJump(it.getStatements().get(0)))
           .forEach(cfgBlock -> {
               EdgeTarget<JavaNode> falseEdge = getEdgeByCond(cfgBlock, EdgeCondition.TRUE.negate());
               if (falseEdge != null) {
                   EdgeTarget<JavaNode> trueEdge = getEdgeByCond(cfgBlock, EdgeCondition.TRUE);
                   if (trueEdge != null && canReachUnconditionally(falseEdge.getBlock(), trueEdge.getBlock())) {
                       List<? extends JavaNode> statements = cfgBlock.getStatements();
                       JavaNode last = statements.get(statements.size() - 1);
                       String type = last instanceof ASTReturnStatement && last.jjtGetNumChildren() == 0
                                     ? "return"
                                     : last instanceof ASTContinueStatement
                                       ? "continue"
                                       : last instanceof ASTBreakStatement
                                         ? "break"
                                         : null;
                       if (type == null) {
                           return;
                       }
                       addViolation(ruleCtx, last, "Unnecessary " + type + " statement");
                   }
               }
           });
    }

    private <N> boolean canReachUnconditionally(BasicBlock<N> start, BasicBlock<N> target) {
        Deque<BasicBlock<N>> todo = new ArrayDeque<>();
        Set<BasicBlock<N>> seen = new HashSet<>();
        todo.push(start);

        while (!todo.isEmpty()) {
            BasicBlock<N> top = todo.pop();
            if (top.equals(target)) {
                return true;
            }

            if (seen.add(top)) {
                for (EdgeTarget<N> outEdge : top.getOutEdges()) {
                    if (outEdge.getCondition() == EdgeCondition.TRUE && outEdge.getBlock().getStatements().isEmpty()) {
                        todo.add(outEdge.getBlock());
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private EdgeTarget<JavaNode> getEdgeByCond(BasicBlock<JavaNode> cfgBlock, EdgeCondition cond) {
        return cfgBlock.getOutEdges().stream().filter(it -> it.getCondition().equals(cond)).findAny().orElse(null);
    }
}
