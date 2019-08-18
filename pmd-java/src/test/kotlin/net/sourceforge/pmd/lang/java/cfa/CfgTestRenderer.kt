/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa

import net.sourceforge.pmd.lang.cfa.BasicBlock
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind.*
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget
import net.sourceforge.pmd.lang.cfa.CfgRenderer
import net.sourceforge.pmd.lang.cfa.FlowGraph
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition
import net.sourceforge.pmd.lang.java.ast.JavaNode
import net.sourceforge.pmd.lang.java.cfa.internal.JavaCfa
import java.io.PrintStream

/**
 * Renderer for tests comparing CFGs to a baseline. The renderer aims to print
 * out the whole structure and isn't meant for anything else. Any change will
 * break many tests.
 *
 * TODO make XML test descriptors, cherry-on-top would be structural comparison
 *   (google graph isomorphism for algos) to be independent of the ID attribution scheme
 *
 */
open class CfgTestRenderer<N>(val cfgDisplayOpts: CfgDisplayOpts) : CfgRenderer<N> {
    override fun render(cfg: FlowGraph<N>, out: PrintStream) {

        val idMap = mutableMapOf<BasicBlock<N>, String>()
        var curId = 0
        val idMaker: (BasicBlock<N>) -> String = {
            idMap.computeIfAbsent(it) {
                when (it.kind) {
                    NORMAL -> curId++.toString()
                    CATCH -> "CATCH " + curId++
                    else -> it.kind.name
                }
            }
        }

        for (block in cfg.depthFirstBlocks().sortedBy(idMaker)) {
            if (block.kind == DANGLING_JUMPS && block.inEdges.isEmpty()) {
                // avoid breaking existing tests, im lazy
                continue
            }
            out.append("+").append(idMaker(block)).println()

            out.println("\tSTATEMENTS")
            block.statements.forEach {
                out.renderNode(it)
            }

            if (cfgDisplayOpts.showLocalErrorHandler) {
                out.println("\tERROR_HANDLER")
                out.append("\t\t").appendln(block.errorHandler?.let(idMaker) ?: "null")
            }

            out.println("\tOUT")
            out.printEdges(block.outEdges, idMaker)
            out.println("\tIN")
            out.printEdges(block.inEdges, idMaker)
        }
    }

    /**
     * Default uses toString, not very explicit.
     */
    protected open fun PrintStream.renderNode(node: N) {
        println("\t\t" + node)
    }

    private fun PrintStream.printEdges(edges: Set<EdgeTarget<N>>, idmaker: (BasicBlock<N>) -> String) {
        for (outEdge in edges.sortedBy { idmaker(it.block) }) {
            print("\t\t-> " + idmaker(outEdge.block))
            if (outEdge.condition != EdgeCondition.TRUE) {
                print(" if " + outEdge.condition + "")
            }
            println()
        }
    }

    /**
     * This keeps a stable algo for sortng blocks, independent from the ordering of
     * [FlowGraph.getBlocks].
     */
    private fun FlowGraph<N>.depthFirstBlocks(): Set<BasicBlock<N>> {

        val stack = mutableListOf<BasicBlock<N>>(start, *otherErrorHandlers.toTypedArray())
        val seen = mutableSetOf<BasicBlock<N>>()

        while (stack.isNotEmpty()) {
            val top = stack.removeAt(stack.size - 1)
            if (seen.add(top)) {
                top.outEdges.forEach { stack += it.block }
            }
        }

        seen += end
        seen += uncaughtExceptionSink
        seen += danglingJumps

        return seen
    }
}

class JavaCfgTestRenderer(cfgDisplayOpts: CfgDisplayOpts) : CfgTestRenderer<JavaNode>(cfgDisplayOpts) {

    override fun PrintStream.renderNode(node: JavaNode) {
        println("\t\t${node.xPathNodeName}:    ${node.text}")
    }

}

// this obscures the name of the class
val JavaCfa: JavaCfa = net.sourceforge.pmd.lang.java.cfa.internal.JavaCfa.INSTANCE


data class CfgDisplayOpts(
        /** If true, displays the local error handler of each node. */
        val showLocalErrorHandler: Boolean = false
)

