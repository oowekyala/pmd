/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal

import net.sourceforge.pmd.lang.cfa.FlowGraph
import net.sourceforge.pmd.lang.java.ast.ASTBlock
import net.sourceforge.pmd.lang.java.ast.JavaNode
import net.sourceforge.pmd.lang.java.cfa.CfgDisplayOpts


abstract class DfaTestSpec(resourcePrefix: String, defaultTestOpts: CfgDisplayOpts = CfgDisplayOpts(), body: DfaTestSpec.() -> Unit) : CfgTestSpec(resourcePrefix, defaultTestOpts, {}) {

    init {
        this.body()
    }

    override fun buildCfg(block: ASTBlock): FlowGraph<JavaNode> =
            JavaCfgBuilder.DFA_INSTANCE.buildCfg(block)


}
