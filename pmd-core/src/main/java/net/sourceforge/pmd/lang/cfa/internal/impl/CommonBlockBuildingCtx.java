/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition.TRUE;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.BlockKind;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;
import net.sourceforge.pmd.lang.cfa.exprs.SymbolicExprFactory;

/**
 * Extension to the base {@link BlockBuildingCtx}. This provides templates
 * for common constructs. This implementation fits Java semantics. The
 * scoping mechanism of break labels is the first thing that jumps to mind
 * when thinking about making eg a PLSQL specific implementation for goto.
 *
 * @param <N> Type of nodes of the built CFG
 * @param <T> Type of the concrete implementation class (F-bound)
 */
public abstract class CommonBlockBuildingCtx<N, T extends CommonBlockBuildingCtx<N, T>> extends BlockBuildingCtx<N, T> {

    // those two maps need to support the null key, it's used to break
    // to the innermost enclosing statement when the break or continue has no label

    /** Targets of continue statements. */
    protected final Map<String, BasicBlock<N>> labeledStarts = new HashMap<>();

    /** Targets of break statements. */
    protected final Map<String, BasicBlock<N>> labeledExits = new HashMap<>();


    protected CommonBlockBuildingCtx(Map<BlockKind, BasicBlock<N>> specialNodes, List<BasicBlock<N>> otherErrorHandlers, BasicBlock<N> before) {
        super(specialNodes, otherErrorHandlers, before);
    }

    protected CommonBlockBuildingCtx() {
        super();
    }

    public abstract SymbolicExprFactory<N> getExprFactory();


    /**
     * Build a new subcontext with the given special nodes,
     * the same in-scope labels as this one, and the given before and after blocks.
     *
     * @param specialNodes Map of special nodes to be used by the sub context. Can't be changed after construction
     * @param otherErrorHandlers Accumulator for roots of error handling code
     */
    protected abstract T makeSubCtx(Map<BlockKind, BasicBlock<N>> specialNodes, List<BasicBlock<N>> otherErrorHandlers, BasicBlock<N> before);

    @Override
    public T subCtx(Map<BlockKind, BasicBlock<N>> specialNodes, BasicBlock<N> before) {
        T subCtx = makeSubCtx(specialNodes, this.otherRoots, before);
        subCtx.labeledExits.putAll(this.labeledExits);
        subCtx.labeledStarts.putAll(this.labeledStarts);
        return subCtx;
    }

    public BasicBlock<N> singleStatementLink(N stmt) {
        BasicBlock<N> b = createBlock(singletonList(stmt));
        linkFromBefore(b, TRUE);
        return b;

        // this doesn't create a new node
        //        before.addStatement(stmt);
        //        linkToAfter(before, TRUE);
    }

    public BasicBlock<N> statementSeqLink(Iterable<? extends N> statements) {
        BasicBlock<N> localBefore = before;

        for (N astStatement : statements) {
            //   The before of one iteration is the after of the previous

            localBefore = subCtx(localBefore).visitTopDown(astStatement);

        }

        return localBefore;
    }

    /**
     * Template for an if-else statement.
     *
     * <pre>{@code
     *
     *     <before>
     *     if (<cond>) <then> else <else>
     *     <after>
     *                 E1                E2        E3
     *    before +---(cond)--> thenStart -> then +--> after
     *       +                                         ^
     *       |        E4                 E5         E6 |
     *       +-----(!cond)---> elseStart -> else +-----+
     *
     *     Without an else:
     *
     *     <before>
     *     if (<cond>) <then>
     *     <after>
     *                   E1              E2        E3
     *    before +---(cond)--> thenStart -> then +--> after
     *       +                                         ^
     *       |        E7                               |
     *       +-----(!cond)-----------------------------+
     *
     * }</pre>
     */
    public BasicBlock<N> ifThenElseLink(EdgeCondition condition, N thenBranch, @Nullable N elseBranch) {

        BasicBlock<N> ifStart = before;

        linkFromBefore(ifStart, TRUE);

        BasicBlock<N> thenStart = createFakeBlock();

        link(ifStart, createTarget(thenStart, condition)); // E1

        BasicBlock<N> join = createFakeBlock();

        // adds E2, E3
        BasicBlock<N> afterE3 = subCtx(thenStart).visitTopDown(thenBranch);
        link(afterE3, createTarget(join, TRUE));

        EdgeCondition notCondition = condition.negate();

        if (elseBranch != null) {
            BasicBlock<N> elseStart = createFakeBlock();

            link(ifStart, createTarget(elseStart, notCondition)); // E4

            // adds E5, E6
            BasicBlock<N> elseEnd = subCtx(elseStart).visitTopDown(elseBranch);

            link(elseEnd, createTarget(join, TRUE));

        } else {
            link(ifStart, createTarget(join, notCondition)); // E7
        }

        return join;
    }

    /**
     * Creates a sub context for a labeled statement. This registers
     * the [exitPoint] under the given name. The subcontext
     * has the same before and after points.
     *
     * <p>The label is only in scope in the subcontext. This is suitable
     * for labeled statements + breaks à la Java. Languages with a goto
     * feature could eg make the label in scope for all after statements.
     */
    public T scopedLabel(String name, BasicBlock<N> exitPoint) {
        T subCtx = subCtx(before);
        subCtx.labeledExits.put(name, exitPoint);
        return subCtx;
    }

    public BasicBlock<N> labeledStatementLink(String name, N statement) {
        BasicBlock<N> exitPoint = createFakeBlock();
        // labeled statements do not execute anything by themselves so we don't keep them
        BasicBlock<N> block = scopedLabel(name, exitPoint).visitTopDown(statement);
        link(block, createTarget(exitPoint, TRUE));
        return exitPoint;
    }

    public BasicBlock<N> continueLink(N stmt, @Nullable String name) {
        BasicBlock<N> start = labeledStarts.get(name);
        if (start == null) {
            // label not found
            start = getDangling();
        }

        return jumpLink(start, stmt);
    }

    /**
     * Break statement (or goto).
     *
     * @param stmt Statement
     * @param name Name of the label jumped to. If null, the break jumps
     *             to the innermost enclosing "breakable" statement
     *             (only loops, with the default impl)
     */
    public BasicBlock<N> breakLink(N stmt, @Nullable String name) {
        BasicBlock<N> exit = labeledExits.get(name);
        if (exit == null) {
            // label not found
            exit = getDangling();
        }

        return jumpLink(exit, stmt);
    }

    /** Return (jump to {@link FlowGraph#getEnd()}). */
    public BasicBlock<N> returnLink(N stmt) {
        return jumpLink(getNormalEnd(), stmt);
    }

    /** Return (jump to {@link FlowGraph#getUncaughtExceptionSink()}). */
    public BasicBlock<N> throwLink(N stmt) {
        return jumpLink(getExceptionSink(), stmt);
    }

    /** Unconditional jump. */
    public BasicBlock<N> jumpLink(BasicBlock<N> jumpTarget, N node) {
        return jumpLink(jumpTarget, node, TRUE);
    }

    /** Conditional jump. */
    public BasicBlock<N> jumpLink(BasicBlock<N> jumpTarget, N node, EdgeCondition condition) {

        BasicBlock<N> block = createBlock(singletonList(node));
        linkFromBefore(block, TRUE);
        link(block, createTarget(jumpTarget, condition));
        BasicBlock<N> after = createFakeBlock();
        link(block, createTarget(after, condition.negate()));
        return after;
    }


    /** Assert statement. */
    public BasicBlock<N> assertLink(EdgeCondition condition, N node, @Nullable N messageNode) {

        BasicBlock<N> ifFalse;
        if (messageNode == null) {
            ifFalse = getExceptionSink();
        } else {
            ifFalse = createBlock(singletonList(messageNode));
            link(ifFalse, createTarget(getExceptionSink(), TRUE)); // E2
        }

        // jump there if
        return jumpLink(ifFalse, node, condition.negate());
    }

    public <C extends N> BasicBlock<N> fallThroughSwitchLink(Iterable<? extends C> switchCases,
                                                             Function<C, EdgeCondition> caseToConditions,
                                                             Function<C, List<? extends N>> caseToStatements,
                                                             Function<C, Boolean> isDefault) {

        /*
            <before>
            switch (<expr>) {
                case <c1>:
                    <b1>
                ...
                case <cn>:
                    <bn>

                default:
                    <deft>
            }
            <after>

            before +--(expr == c1)----------------------> b1
                   |                                      +
                   |                                      |
                   |                                      v
                   +--(expr == c2)----------------------> b2
                   ...                                    ..
                   |                                      |
                   |                                      v
                   +--(expr == cn)----------------------> bn
                   |                                      |
                   |                                      v
                   +--(expr != c1 && .. && expr != cn)-> deft +---> after

            We know c1 != c2 .. != cn so we don't need to add the negated conditions along each path
            Also, complete fallthrough paths don't give rise to parallel edges. For example:
            switch (<e>) {
                case 1:
                case 2:
                    foo();
            }

            will give rise to a single edge with condition (e == 1 || e == 2)

         */

        boolean empty = true;

        BasicBlock<N> localBefore = createFakeBlock();

        EdgeCondition defaultCond = TRUE;

        EdgeCondition fallthroughCond = TRUE.negate();

        BasicBlock<N> after = createFakeBlock();

        for (Iterator<? extends C> iterator = switchCases.iterator(); iterator.hasNext();) {
            C caseStmt = iterator.next();
            empty = false; // at least one case

            List<? extends N> stmts = caseToStatements.apply(caseStmt);

            fallthroughCond = fallthroughCond.or(caseToConditions.apply(caseStmt));

            if (stmts.isEmpty()) {
                continue;
            }

            if (!iterator.hasNext() && isDefault.apply(caseStmt)) {
                // default
                link(this.before, createTarget(localBefore, defaultCond));
            } else {
                // non fallthrough
                link(this.before, createTarget(localBefore, fallthroughCond));
                defaultCond = defaultCond.and(fallthroughCond.negate());
                fallthroughCond = TRUE.negate();
            }

            T subCtx = subCtx(localBefore);
            subCtx.labeledExits.put(null, after); // breaks jump to the after of this ctx

            BasicBlock<N> localAfter = subCtx.statementSeqLink(stmts);

            localBefore = createFakeBlock();

            link(localAfter, createTarget(localBefore, TRUE));
        }


        link(localBefore, createTarget(after, TRUE));

        // todo here we return before, yet the expression being evaluated has side-effects
        return empty ? before : after;
    }

    public BasicBlock<N> tryLink(N body, Iterable<? extends N> resources, Iterable<? extends N> catchStmts, @Nullable N finallyBlock) {

        /*
            <before>
            try (<resources>)
                <body>
            catch (<t1> e) <c1>
            catch (<t2> e) <c2>
            ..
            catch (<tn> e) <cn>
            finally <finally>

            <after>

            In a try, we change the exception sink to link to the subgraph of the catch list.
            The exception sink looks like so:

            newExceptionSink +-----(e <: t1) --> c1 +---------------------------> finally +------------> after
                             |
                             |
                             +-----!(e <: t1)-> D1  +-- (e <: t2) --> c2 +----> finally +------> after
                                                +
                                                |
                                                +------- ...
                                                  ...
                                                   +
                                                   |
                                                   +---- !(e <: tn) --------> finally ------> exceptionSink


            where the symbol '<:' is shorthand for 'instanceof'

            We need add some effect annotations to the edges to represent cases where the finally was
            linked to after abrupt completion (return or throw).

        */


        BasicBlock<N> newExceptionSink = createBlock(BlockKind.CATCH, emptyList());
        otherRoots.add(newExceptionSink);

        // either the normal start of the finally, or the end point
        BasicBlock<N> normalAfterPoint = createFakeBlock();
        BasicBlock<N> exceptionFinallyStart = finallyBlock == null ? getExceptionSink() : createFakeBlock();
        BasicBlock<N> returnFinallyStart = finallyBlock == null ? getNormalEnd() : createFakeBlock();

        // either the end of the switch, or the end of the finally
        BasicBlock<N> afterAll = normalAfterPoint;

        // this is true if there is no catch stmts
        BasicBlock<N> bottom = linkCatchStmts(catchStmts, exceptionFinallyStart, returnFinallyStart, normalAfterPoint, newExceptionSink);

        //  bottom is the node for uncaught exception
        link(bottom, createTarget(exceptionFinallyStart, TRUE));

        if (finallyBlock != null) {
            // ie there is a finally block

            BasicBlock<N> realFinallyStart = createFakeBlock();

            BasicBlock<N> finallyEnd = subCtx(realFinallyStart).visitTopDown(finallyBlock);

            link(exceptionFinallyStart, createTarget(realFinallyStart, getExprFactory().pendingThrow()));
            link(returnFinallyStart, createTarget(realFinallyStart, getExprFactory().pendingReturn()));
            link(normalAfterPoint, createTarget(realFinallyStart, TRUE));

            afterAll = createFakeBlock();

            // if return pending -> END
            link(finallyEnd, createTarget(getNormalEnd(), getExprFactory().ifPendingReturn()));
            // if throw pending -> ERROR
            link(finallyEnd, createTarget(getExceptionSink(), getExprFactory().ifPendingThrow()));
            // else -> after
            link(finallyEnd, createTarget(afterAll, getExprFactory().ifPendingThrow().negate().and(getExprFactory().ifPendingReturn().negate())));
        }

        // link body - exceptions return to the exception sink tree

        EnumMap<BlockKind, BasicBlock<N>> bodySpecials = buildSpecialMap(newExceptionSink, returnFinallyStart);

        BasicBlock<N> resourceEnd = subCtx(bodySpecials, before).statementSeqLink(resources);

        BasicBlock<N> bodyEnd = subCtx(bodySpecials, resourceEnd).visitTopDown(body);

        link(bodyEnd, createTarget(normalAfterPoint, TRUE));

        return afterAll;
    }

    protected BasicBlock<N> linkCatchStmts(Iterable<? extends N> catchStmts,
                                           BasicBlock<N> exceptionFinallyStart,
                                           BasicBlock<N> returnFinallyStart,
                                           BasicBlock<N> normalFinallyStart,
                                           final BasicBlock<N> newExceptionSink) {

        // Yield control to the finally always.
        final EnumMap<BlockKind, BasicBlock<N>> catchSpecials = buildSpecialMap(exceptionFinallyStart, returnFinallyStart);

        BasicBlock<N> curSequencePoint = newExceptionSink;

        for (N catchStmt : catchStmts) {
            EdgeCondition cond = getExprFactory().catchMatches(getConditionNodeOfCatch(catchStmt));


            BasicBlock<N> nextSequencePoint = createFakeBlock();

            BasicBlock<N> catchBlock = createFakeBlock();

            link(curSequencePoint, catchBlock.asMutable().createEdge(cond));
            link(curSequencePoint, nextSequencePoint.asMutable().createEdge(cond.negate()));

            BasicBlock<N> endCatch = subCtx(catchSpecials, catchBlock).visitTopDown(catchStmt);
            link(endCatch, createTarget(normalFinallyStart, TRUE));

            curSequencePoint = nextSequencePoint;
        }

        return curSequencePoint;
    }

    protected N getConditionNodeOfCatch(N catchStmt) {
        return catchStmt;
    }

    @NonNull
    private EnumMap<BlockKind, BasicBlock<N>> buildSpecialMap(BasicBlock<N> newExceptionSink, BasicBlock<N> newReturnEnd) {
        EnumMap<BlockKind, BasicBlock<N>> newSpecials = new EnumMap<>(getSpecialNodes());
        // we use ERROR here even though the kind of the node is CATCH...
        newSpecials.put(BlockKind.ERROR, newExceptionSink);
        // le body peut contenir des return qui sont préemptés par le finally
        //  donc les return pointent vers le finally
        newSpecials.put(BlockKind.END, newReturnEnd);
        return newSpecials;
    }



    /*

     -------FOR STATEMENT

            <before>
            for (<init>; <cond>; <update>) <body>
            <after>

                    E1              E2                  E3          E4                 E5
           before +-----> init +---(cond)---> loopStart ---> body +---> update +---(!cond)--> after
                           +                  ^                         +                    ^
                           |                  |      E6                 |                    |
                           |                  +-----(cond)--------------+                    |
                           |                      E7                                         |
                           +--------------------(!cond)--------------------------------------+


     -------REDUCTIONS

            CFGs of other kinds of loops very much look like the
            one of the for and so we reduce them to a for (with some tweaks):

            while (<cond>) <body>
                ~> for(;<cond>;) <body>

            do <body> while (<cond>);
                ~> for(;<cond>;) <body>
                (where E2 is unconditional, and E7 is omitted)

            for (<assignment> : <iterable>) <body>
                ~> for(<iterable>; <iterable>.hasNext();) {
                    <assignment>
                    <body>
                }


     -------FOREACH STATEMENT

            <before>
            for (<loopStart> : <iterable>) <body>
            <after>

           before +----->iterable+---hasNext--->loopStart--->body+--->emptyUpdate+---!hasNext--> after
                           +                      ^                      +                         ^
                           |                      |                      |                         |
                           |                      +--------hasNext-------+                         |
                           |                                                                       |
                           +--------------------!hasNext-------------------------------------------+


     -------WHILE STATEMENT

            <before>
            while (<cond>) <body>
            <after>

           before +-----cond---> loopStart +--> body +---> emptyUpdate +---!cond----> after
              +                      ^                     +                           ^
              |                      |                     |                           |
              |                      +--------cond---------+                           |
              |                                                                        |
              +------------------------------!cond-------------------------------------+


     -------DO STATEMENT


            <before>
            do <body> while (<cond>);
            <after>

           before +--->loopStart--->body+---> emptyUpdate +---!cond----> after
                             ^                      +
                             |                      |
                             +--------cond----------+

    */

    public BasicBlock<N> makeLoop(EdgeCondition condition,
                                  boolean isDoLoop,
                                  List<? extends N> initStmts,
                                  List<? extends N> loopStartStmts,
                                  List<? extends N> updateStmts,
                                  String loopName,
                                  N body
    ) {

        BasicBlock<N> init = createBlock(initStmts);
        linkFromBefore(init, TRUE); // E1

        EdgeCondition notCondition = condition.negate();

        BasicBlock<N> update = createBlock(updateStmts);


        // fake start node for the body
        BasicBlock<N> loopStart = createBlock(loopStartStmts);
        EdgeTarget<N> reloop = createTarget(loopStart, condition);

        // E2
        if (isDoLoop) {
            link(init, loopStart.asMutable().createEdge(TRUE));
        } else {
            link(init, reloop);
        }

        BasicBlock<N> after = createFakeBlock();

        link(update, createTarget(after, notCondition)); // E5
        link(update, reloop); // E6
        if (!isDoLoop) {
            link(init, createTarget(after, notCondition)); // E7
        }

        T bodyCtx = subLoopCtx(loopStart, update, after, loopName);


        // Adds E3, E4 as needed
        BasicBlock<N> bodyEnd = bodyCtx.visitTopDown(body);

        link(bodyEnd, createTarget(update, TRUE));

        return after;
    }


    /**
     * Creates a sub context for a loop.
     *  @param beforeIteration Block from which control comes before an iteration.
     *                        For example in case of for loops this is the update statement.
     * @param afterIteration  Block to which control yields after a normal iteration.
     *                        This can be the update statement of a for loop, or just
     *                        the start of the next iteration for while loops
     * @param after           Block representing the loop break
     * @param name            Name of the loop, or null if it is unnamed
     */
    public T subLoopCtx(BasicBlock<N> beforeIteration,
                        BasicBlock<N> afterIteration,
                        BasicBlock<N> after,
                        @Nullable String name) {

        T subCtx = subCtx(beforeIteration);
        subCtx.labeledStarts.put(name, afterIteration);
        subCtx.labeledExits.put(name, after);
        if (name != null) {
            // this makes it so that this loop is accessible with the null label
            // it overwrites previous loops
            subCtx.labeledStarts.put(null, afterIteration);
            subCtx.labeledExits.put(null, after);
        }
        return subCtx;
    }


}
