/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * Generic control-flow analysis (CFA) framework.
 *
 * <p>This is a partial reimplementation of the sibling {@code dfa} package.
 * The difference is that this package focuses on CFA, not on data-flow analysis (yet).
 * The key model object for CFA is the {@linkplain net.sourceforge.pmd.lang.cfa.FlowGraph control-flow
 * graph} (CFG).
 * It models transitions of control flow between statements. DFA reasons
 * about the possible values of variables - which is only possible with
 * the help of a CFG. But let's not mix the two.
 *
 * <h3>Roadmap</h3>
 *
 * <ul>
 * <li>Finer representation of exceptional completion (throw, assert)
 * needs a better symbol resolver and type inspection framework.
 * It would be super nice to implement rules like ExceptionAsFlowControl
 * using a CFG, and this would open the way to new rules, such as one
 * detecting statements that may be moved outside a try statement.
 * </li>
 * <li>Implementing a useful DFA framework on top of this framework is the end goal.
 * The current rule DFAAnomalyAnalysis should be split and its useful use cases
 * (DU, DD) reimplemented using a CFG as a PoC.
 * </li>
 * </ul>
 */

package net.sourceforge.pmd.lang.cfa;
