/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import static net.sourceforge.pmd.util.CollectionUtil.asSingle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTArrayAllocation;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.AccessType;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;
import net.sourceforge.pmd.lang.java.ast.ASTCatchClause;
import net.sourceforge.pmd.lang.java.ast.ASTCatchParameter;
import net.sourceforge.pmd.lang.java.ast.ASTCompactConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConditionalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTContinueStatement;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate;
import net.sourceforge.pmd.lang.java.ast.ASTForeachStatement;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTLabeledStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLoopStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTResourceList;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchArrowBranch;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchBranch;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchLike;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSynchronizedStatement;
import net.sourceforge.pmd.lang.java.ast.ASTThisExpression;
import net.sourceforge.pmd.lang.java.ast.ASTThrowStatement;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTUnaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.ast.ASTYieldStatement;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.ast.InvocationNode;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.ast.QualifiableExpression;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JConstructorSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFieldSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFormalParamSymbol;
import net.sourceforge.pmd.lang.java.symbols.JLocalVariableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.OptionalBool;

/**
 * A reaching definition analysis. This may be used to check whether
 * eg a value escapes, or is overwritten on all code paths.
 */
public final class DataflowForwardAnalysis {

    private DataflowForwardAnalysis() {
        // utility class
    }


    private static ForwardAnalysisResult process(ASTCompilationUnit node) {

        ForwardAnalysisResult dataflowResult = new ForwardAnalysisResult();
        for (ASTTypeDeclaration typeDecl : node.getTypeDeclarations()) {
            GlobalAlgoState subResult = new GlobalAlgoState();
            ForwardDataflowVisitor.processTypeDecl(typeDecl, new SpanInfo(subResult));
            if (subResult.usedAssignments.size() < subResult.allAssignments.size()) {
                Set<ValueFacts> unused = subResult.allAssignments;
                unused.removeAll(subResult.usedAssignments);
                unused.removeIf(ValueFacts::isUnbound);
                unused.removeIf(ValueFacts::isFieldDefaultValue);
                dataflowResult.unusedAssignments.addAll(unused);
            }

            CollectionUtil.mergeMaps(
                dataflowResult.killRecord,
                subResult.killRecord,
                (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
        }

        return dataflowResult;
    }

    static class ForwardAnalysisResult {

    }

    private static final class ForwardDataflowVisitor extends JavaVisitorBase<SpanInfo, SpanInfo> {

        // The class scope for the "this" reference, used to find fields
        // of this class
        private final @NonNull JClassSymbol enclosingClassScope;
        private final boolean inStaticCtx;

        private ForwardDataflowVisitor(@NonNull JClassSymbol scope, boolean inStaticCtx) {
            this.enclosingClassScope = scope;
            this.inStaticCtx = inStaticCtx;
        }

        /**
         * If true, we're also tracking fields of the {@code this} instance,
         * because we're in a ctor or initializer, or instance method.
         */
        private boolean trackThisInstance() {
            return !inStaticCtx;
        }

        // following deals with control flow structures

        @Override
        protected SpanInfo visitChildren(Node node, SpanInfo data) {
            for (Node child : node.children()) {
                // each output is passed as input to the next (most relevant for blocks)
                data = child.acceptVisitor(this, data);
            }
            return data;
        }

        @Override
        public SpanInfo visit(ASTBlock node, final SpanInfo data) {
            return processBreakableStmt(node, data, () -> {
                // variables local to a loop iteration must be killed before the
                // next iteration

                SpanInfo state = data;
                List<ASTVariableId> localsToKill = new ArrayList<>(0);

                for (JavaNode child : node.children()) {
                    // each output is passed as input to the next (most relevant for blocks)
                    state = acceptOpt(child, state);
                    if (child instanceof ASTLocalVariableDeclaration) {
                        for (ASTVariableId id : (ASTLocalVariableDeclaration) child) {
                            localsToKill.add(id);
                        }
                    }
                }

                for (ASTVariableId var : localsToKill) {
                    state.deleteVar(var.getSymbol());
                }

                return state;
            });
        }

        @Override
        public SpanInfo visit(ASTSwitchStatement node, SpanInfo data) {
            return processSwitch(node, data);
        }

        @Override
        public SpanInfo visit(ASTSwitchExpression node, SpanInfo data) {
            return processSwitch(node, data);
        }

        private SpanInfo processSwitch(ASTSwitchLike switchLike, SpanInfo data) {
            GlobalAlgoState global = data.global;
            SpanInfo before = acceptOpt(switchLike.getTestedExpression(), data);

            SpanInfo breakTarget = before.fork();
            global.breakTargets.push(breakTarget);
            accLabels(switchLike, global, breakTarget, null);

            // If switch non-total then there is a path where the switch completes normally
            // (value not matched).
            // Todo make that an attribute of ASTSwitchLike, check for totality when pattern matching is involved
            boolean isTotal = switchLike.hasDefaultCase()
                              || switchLike instanceof ASTSwitchExpression
                              || switchLike.isExhaustiveEnumSwitch();

            PSet<SpanInfo> successors = HashTreePSet.empty();
            boolean allBranchesCompleteAbruptly = true;
            SpanInfo current = before;
            for (ASTSwitchBranch branch : switchLike.getBranches()) {
                if (branch instanceof ASTSwitchArrowBranch) {
                    current = acceptOpt(((ASTSwitchArrowBranch) branch).getRightHandSide(), before.fork());
                    current = global.breakTargets.doBreak(current, null); // process this as if it was followed by a break
                } else {
                    // fallthrough branch
                    current = acceptOpt(branch, before.fork().absorb(current));
                    OptionalBool isFallingThrough = current.hasCompletedAbruptly.complement();
                    branch.getUserMap().set(SWITCH_BRANCH_FALLS_THROUGH, isFallingThrough);
                    successors = CollectionUtil.union(successors, current.abruptCompletionTargets);
                    allBranchesCompleteAbruptly &= current.hasCompletedAbruptly.isTrue();

                    if (isFallingThrough == OptionalBool.NO) {
                        current = before.fork();
                    }
                }
            }

            before = global.breakTargets.pop();

            PSet<@Nullable SpanInfo> externalTargets = successors.minus(before);
            OptionalBool switchCompletesAbruptly;
            if (isTotal && allBranchesCompleteAbruptly && externalTargets.equals(successors)) {
                // then all branches complete abruptly, and none of them because of a break to this switch
                switchCompletesAbruptly = OptionalBool.YES;
            } else if (successors.isEmpty()
                       || asSingle(successors) == breakTarget) { // NOPMD CompareObjectsWithEqual this is what we want
                // then the branches complete normally, or they just break the switch
                switchCompletesAbruptly = OptionalBool.NO;
            } else {
                switchCompletesAbruptly = OptionalBool.UNKNOWN;
            }

            // join with the last state, which is the exit point of the
            // switch, if it's not closed by a break;
            SpanInfo result = before.absorb(current);
            result.hasCompletedAbruptly = switchCompletesAbruptly;
            result.abruptCompletionTargets = externalTargets;
            return result;
        }

        @Override
        public SpanInfo visit(ASTIfStatement node, SpanInfo data) {
            return processBreakableStmt(node, data, () -> makeConditional(data, node.getCondition(), node.getThenBranch(), node.getElseBranch()));
        }

        @Override
        public SpanInfo visit(ASTConditionalExpression node, SpanInfo data) {
            return makeConditional(data, node.getCondition(), node.getThenBranch(), node.getElseBranch());
        }

        SpanInfo makeConditional(SpanInfo before, ASTExpression condition, JavaNode thenBranch, JavaNode elseBranch) {
            SpanInfo thenState = before.fork();
            SpanInfo elseState = elseBranch != null ? before.fork() : before;

            linkConditional(before, condition, thenState, elseState, true);

            thenState = acceptOpt(thenBranch, thenState);
            elseState = acceptOpt(elseBranch, elseState);

            return elseState.absorb(thenState);
        }

        /*
         * This recursive procedure translates shortcut conditionals
         * that occur in condition position in the following way:
         *
         * if (a || b) <then>                  if (a)      <then>
         * else <else>               ~>        else
         *                                       if (b)    <then>
         *                                       else      <else>
         *
         *
         * if (a && b) <then>                  if (a)
         * else <else>               ~>          if (b)    <then>
         *                                       else      <else>
         *                                     else        <else>
         *
         * The new conditions are recursively processed to translate
         * bigger conditions, like `a || b && c`
         *
         * This is how it works, but the <then> and <else> branch are
         * visited only once, because it's not done in this method, but
         * in makeConditional.
         *
         * @return the state in which all expressions have been evaluated
         *      Eg for `a || b`, this is the `else` state (all evaluated to false)
         *      Eg for `a && b`, this is the `then` state (all evaluated to true)
         *
         */
        private SpanInfo linkConditional(SpanInfo before, ASTExpression condition, SpanInfo thenState, SpanInfo elseState, boolean isTopLevel) {
            if (condition == null) {
                return before;
            }

            if (condition instanceof ASTInfixExpression) {
                BinaryOp op = ((ASTInfixExpression) condition).getOperator();
                if (op == BinaryOp.CONDITIONAL_OR) {
                    return visitShortcutOrExpr((ASTInfixExpression) condition, before, thenState, elseState);
                } else if (op == BinaryOp.CONDITIONAL_AND) {
                    // To mimic a shortcut AND expr, swap the thenState and the elseState
                    // See explanations in method
                    return visitShortcutOrExpr((ASTInfixExpression) condition, before, elseState, thenState);
                }
            }

            SpanInfo state = acceptOpt(condition, before);
            if (isTopLevel) {
                thenState.absorb(state);
                elseState.absorb(state);
            }
            return state;
        }

        SpanInfo visitShortcutOrExpr(ASTInfixExpression orExpr,
                                     SpanInfo before,
                                     SpanInfo thenState,
                                     SpanInfo elseState) {

            //  if (<a> || <b> || ... || <n>) <then>
            //  else <else>
            //
            // in <then>, we are sure that at least <a> was evaluated,
            // but really any prefix of <a> ... <n> is possible so they're all merged

            // in <else>, we are sure that all of <a> ... <n> were evaluated (to false)

            // If you replace || with &&, then the above holds if you swap <then> and <else>
            // So this method handles the OR expr, the caller can swap the arguments to make an AND

            // ---
            // This method side effects on thenState and elseState to
            // set the variables.

            SpanInfo cur = before;
            cur = linkConditional(cur, orExpr.getLeftOperand(), thenState, elseState, false);
            thenState.absorb(cur);
            cur = linkConditional(cur, orExpr.getRightOperand(), thenState, elseState, false);
            thenState.absorb(cur);

            elseState.absorb(cur);

            return cur;
        }

        @Override
        public SpanInfo visit(ASTSynchronizedStatement node, SpanInfo data) {
            return processBreakableStmt(node, data, () -> {
                // visit lock expr and child block
                SpanInfo body = super.visit(node, data);
                // We should assume that all assignments may be observed by other threads
                // at the end of the critical section.
                useAllSelfFields(body, JavaAstUtils.isInStaticCtx(node), enclosingClassScope);
                return body;
            });
        }

        @Override
        public SpanInfo visit(ASTTryStatement node, final SpanInfo before) {

            /*
                <before>
                try (<resources>) {
                    <body>
                } catch (IOException e) {
                    <catch>
                } finally {
                    <finally>
                }
                <end>

                There is a path      <before> -> <resources> -> <body> -> <finally> -> <end>
                and for each catch,  <before> -> <catch> -> <finally> -> <end>

                Except that abrupt completion before the <finally> jumps
                to the <finally> and completes abruptly for the same
                reason (if the <finally> completes normally), which
                means it doesn't go to <end>
             */
            ASTFinallyClause finallyClause = node.getFinallyClause();

            SpanInfo finalState = processBreakableStmt(node, before, () -> {

                if (finallyClause != null) {
                    before.myFinally = before.forkEmpty();
                }

                final List<ASTCatchClause> catchClauses = node.getCatchClauses().toList();
                final List<SpanInfo> catchSpans = catchClauses.isEmpty() ? Collections.emptyList()
                                                                         : new ArrayList<>();

                // pre-fill catch spans
                for (int i = 0; i < catchClauses.size(); i++) {
                    catchSpans.add(before.forkEmpty());
                }

                @Nullable ASTResourceList resources = node.getResources();

                SpanInfo bodyState = before.fork();
                bodyState = bodyState.withCatchBlocks(catchSpans);
                bodyState = acceptOpt(resources, bodyState);
                bodyState = acceptOpt(node.getBody(), bodyState);
                bodyState = bodyState.withCatchBlocks(Collections.emptyList());

                SpanInfo exceptionalState = null;
                int i = 0;
                for (ASTCatchClause catchClause : node.getCatchClauses()) {
                    /*
                        Note: here we absorb the end state of the body, which is not necessary.
                        We do that to conform to the language's definition of "effective-finality",
                        which is more conservative than needed. Doing this fixes FPs in LocalVariableCouldBeFinal
                        at the cost of some FNs in UnusedAssignment.
                     */
                    SpanInfo catchSpan = catchSpans.get(i);
                    catchSpan.absorb(bodyState);

                    SpanInfo current = acceptOpt(catchClause, catchSpan);
                    exceptionalState = current.absorb(exceptionalState);
                    i++;
                }
                return bodyState.absorb(exceptionalState);
            });

            if (finallyClause != null) {
                if (finalState.abruptCompletionTargets.contains(finalState.returnOrThrowTarget)) {
                    // this represents the finally clause when it was entered
                    // because of abrupt completion
                    // since we don't know when it terminated we must join it with before
                    SpanInfo abruptFinally = before.myFinally.absorb(before);
                    acceptOpt(finallyClause, abruptFinally);
                    before.myFinally = null;
                    abruptFinally.abruptCompletionByThrow(false); // propagate to enclosing catch/finallies
                }

                // this is the normal finally
                finalState = acceptOpt(finallyClause, finalState);
                // then all break targets are successors of the finally
                for (SpanInfo target : finalState.abruptCompletionTargets) {
                    // Then there is a return or throw within the try or catch blocks.
                    // Control first passes to the finally, then tries to get out of the function
                    // (stopping on finally).
                    // before.myFinally = null;
                    //finalState.abruptCompletionByThrow(false); // propagate to enclosing catch/finallies
                    target.absorb(finalState);
                }
            }

            // In the 7.0 grammar, the resources should be explicitly
            // used here. For now they don't trigger anything as their
            // node is not a VariableId. There's a test to
            // check that.

            return finalState;
        }

        @Override
        public SpanInfo visit(ASTCatchClause node, SpanInfo data) {
            SpanInfo result = visitJavaNode(node, data);
            result.deleteVar(node.getParameter().getVarId().getSymbol());
            return result;
        }

        @Override
        public SpanInfo visit(ASTLambdaExpression node, SpanInfo data) {
            // Lambda expression have control flow that is separate from the method
            // So we fork the context, but don't join it

            // Reaching definitions of the enclosing context still reach in the lambda
            // Since those definitions are [effectively] final, they actually can't be
            // killed, but they can be used in the lambda

            SpanInfo before = data;

            JavaNode lambdaBody = node.getChild(node.getNumChildren() - 1);
            // if it's an expression, then no assignments may occur in it,
            // but it can still use some variables of the context
            acceptOpt(lambdaBody, before.forkCapturingNonLocal());
            return before;
        }

        @Override
        public SpanInfo visit(ASTWhileStatement node, SpanInfo data) {
            return handleLoop(node, data, null, node.getCondition(), null, node.getBody(), true, null);
        }

        @Override
        public SpanInfo visit(ASTDoStatement node, SpanInfo data) {
            return handleLoop(node, data, null, node.getCondition(), null, node.getBody(), false, null);
        }

        @Override
        public SpanInfo visit(ASTForeachStatement node, SpanInfo data) {
            ASTStatement body = node.getBody();
            ASTExpression init = node.getIterableExpr();
            return handleLoop(node, data, init, null, null, body, true, node.getVarId());
        }

        @Override
        public SpanInfo visit(ASTForStatement node, SpanInfo data) {
            ASTStatement body = node.getBody();
            ASTForInit init = node.firstChild(ASTForInit.class);
            ASTExpression cond = node.getCondition();
            ASTForUpdate update = node.firstChild(ASTForUpdate.class);
            return handleLoop(node, data, init, cond, update, body, true, null);
        }


        private SpanInfo handleLoop(ASTLoopStatement loop,
                                    SpanInfo before,
                                    JavaNode init,
                                    ASTExpression cond,
                                    JavaNode update,
                                    ASTStatement body,
                                    boolean checkFirstIter,
                                    ASTVariableId foreachVar) {

            //todo while(true) and do {}while(true); are special-cased
            // by the compiler and there is no fork

            SpanInfo breakTarget = before.forkEmpty();
            SpanInfo continueTarget = before.forkEmpty();
            pushTargets(loop, breakTarget, continueTarget);

            // perform a few "iterations", to make sure that assignments in
            // the body can affect themselves in the next iteration, and
            // that they affect the condition, etc

            before = acceptOpt(init, before);
            if (checkFirstIter && cond != null) { // false for do-while
                SpanInfo ifcondTrue = before.forkEmpty();
                linkConditional(before, cond, ifcondTrue, breakTarget, true);
                before = ifcondTrue;
            }

            if (foreachVar != null) {
                // in foreach loops, the loop variable is assigned before the first iteration
                before.assign(foreachVar.getSymbol(), foreachVar);
            }


            // make the defs of the body reach the other parts of the loop,
            // including itself
            SpanInfo iter = acceptOpt(body, before.fork());

            // Make assignments that reached a continue reach the condition block before next iteration
            iter.absorb(continueTarget);

            if (foreachVar != null && iter.hasVar(foreachVar)) {
                // in foreach loops, the loop variable is reassigned on each update
                iter.assign(foreachVar.getSymbol(), foreachVar);
            } else {
                iter = acceptOpt(update, iter);
            }

            linkConditional(iter, cond, iter, breakTarget, true);
            // do a second round to make sure assignments can reach themselves.
            iter = acceptOpt(body, iter);

            SpanInfo result = popTargets(loop, breakTarget, continueTarget);
            result.absorb(iter);
            if (checkFirstIter) {
                // if the first iteration is checked,
                // then it could be false on the first try, meaning
                // the definitions before the loop reach after too
                result.absorb(before);
            }

            if (foreachVar != null) {
                result.deleteVar(foreachVar.getSymbol());
            }

            // These targets are now obsolete
            result.abruptCompletionTargets =
                result.abruptCompletionTargets.minus(breakTarget).minus(continueTarget);
            return result;
        }

        /**
         * Process a statement that may be broken out of if it is annotated with a label.
         * This is theoretically all statements, as all of them may be annotated. However,
         * some statements may not contain a break. Eg if a return statement has a label,
         * the label can never be used. The weirdest example is probably an annotated break
         * statement, which may break out of itself.
         *
         * <p>Try statements are handled specially because of the finally.
         */
        private SpanInfo processBreakableStmt(ASTStatement statement, SpanInfo input, Supplier<SpanInfo> processFun) {
            if (!(statement.getParent() instanceof ASTLabeledStatement)) {
                // happy path, no labels
                return processFun.get();
            }
            GlobalAlgoState globalState = input.global;
            // this will be filled with the reaching defs of the break statements, then merged with the actual exit state
            SpanInfo placeholderForExitState = input.forkEmpty();

            PSet<String> labels = accLabels(statement, globalState, placeholderForExitState, null);
            SpanInfo endState = processFun.get();

            // remove the labels
            globalState.breakTargets.namedTargets.keySet().removeAll(labels);
            SpanInfo result = endState.absorb(placeholderForExitState);
            result.abruptCompletionTargets = result.abruptCompletionTargets.minus(placeholderForExitState);
            return result;
        }

        private static PSet<String> accLabels(JavaNode statement, GlobalAlgoState globalState, SpanInfo breakTarget, @Nullable SpanInfo continueTarget) {
            Node parent = statement.getParent();
            PSet<String> labels = HashTreePSet.empty();
            // collect labels and give a name to the exit state.
            while (parent instanceof ASTLabeledStatement) {
                String label = ((ASTLabeledStatement) parent).getLabel();
                labels = labels.plus(label);
                globalState.breakTargets.namedTargets.put(label, breakTarget);
                if (continueTarget != null) {
                    globalState.continueTargets.namedTargets.put(label, continueTarget);
                }
                parent = parent.getParent();
            }
            return labels;
        }

        private void pushTargets(ASTLoopStatement loop, SpanInfo breakTarget, SpanInfo continueTarget) {
            GlobalAlgoState globalState = breakTarget.global;
            accLabels(loop, globalState, breakTarget, continueTarget);
            globalState.breakTargets.unnamedTargets.push(breakTarget);
            globalState.continueTargets.unnamedTargets.push(continueTarget);
        }

        private SpanInfo popTargets(ASTLoopStatement loop, SpanInfo breakTarget, SpanInfo continueTarget) {
            GlobalAlgoState globalState = breakTarget.global;
            globalState.breakTargets.unnamedTargets.pop();
            globalState.continueTargets.unnamedTargets.pop();

            SpanInfo total = breakTarget.absorb(continueTarget);

            Node parent = loop.getParent();
            while (parent instanceof ASTLabeledStatement) {
                String label = ((ASTLabeledStatement) parent).getLabel();
                total = total.absorb(globalState.breakTargets.namedTargets.remove(label));
                total = total.absorb(globalState.continueTargets.namedTargets.remove(label));
                parent = parent.getParent();
            }
            return total;
        }

        private SpanInfo acceptOpt(JavaNode node, SpanInfo before) {
            return node == null ? before : node.acceptVisitor(this, before);
        }

        @Override
        public SpanInfo visit(ASTContinueStatement node, SpanInfo data) {
            return data.global.continueTargets.doBreak(data, node.getImage());
        }

        @Override
        public SpanInfo visit(ASTBreakStatement node, SpanInfo data) {
            return processBreakableStmt(node, data, () -> data.global.breakTargets.doBreak(data, node.getImage()));
        }

        @Override
        public SpanInfo visit(ASTYieldStatement node, SpanInfo data) {
            super.visit(node, data); // visit expression

            // treat as break, ie abrupt completion + link reaching defs to outer context
            return data.global.breakTargets.doBreak(data, null);
        }


        // both of those exit the scope of the method/ctor, so their assignments go dead

        @Override
        public SpanInfo visit(ASTThrowStatement node, SpanInfo data) {
            super.visit(node, data);
            return data.abruptCompletionByThrow(false);
        }

        @Override
        public SpanInfo visit(ASTReturnStatement node, SpanInfo data) {
            super.visit(node, data);
            return data.abruptCompletion(data.returnOrThrowTarget);
        }

        // following deals with assignment

        @Override
        public SpanInfo visit(ASTFormalParameter node, SpanInfo data) {
            data.declareBlank(node.getVarId());
            return data;
        }

        @Override
        public SpanInfo visit(ASTCatchParameter node, SpanInfo data) {
            data.declareBlank(node.getVarId());
            return data;
        }

        @Override
        public SpanInfo visit(ASTVariableDeclarator node, SpanInfo data) {
            JVariableSymbol var = node.getVarId().getSymbol();
            ASTExpression rhs = node.getInitializer();
            if (rhs != null) {
                rhs.acceptVisitor(this, data);
                data.assign(var, rhs);
            } else if (isAssignedImplicitly(node.getVarId())) {
                data.declareBlank(node.getVarId());
            }
            return data;
        }

        @Override
        public SpanInfo visit(ASTCompactConstructorDeclaration node, SpanInfo data) {
            // Visiting a method/ctor declaration declares formal parameters.
            // In the compact ctor decl there is no formal parameter nodes, so
            // we need to declare these explicitly
            JConstructorSymbol ctorSym = node.getSymbol();
            for (JFormalParamSymbol formal : ctorSym.getFormalParameters()) {
                ASTVariableId varId = formal.tryGetNode();
                assert varId != null && varId.isRecordComponent();
                data.assign(formal, varId);
            }
            // visit the body
            data = super.visit(node, data);

            return data;
        }

        /**
         * Whether the variable has an implicit initializer, that is not
         * an expression. For instance, formal parameters have a value
         * within the method, same for exception parameters, foreach variables,
         * fields (default value), etc. Only blank local variables have
         * no initial value.
         */
        private boolean isAssignedImplicitly(ASTVariableId var) {
            return !var.isLocalVariable() || var.isForeachVariable();
        }

        @Override
        public SpanInfo visit(ASTUnaryExpression node, SpanInfo data) {
            data = acceptOpt(node.getOperand(), data);

            if (node.getOperator().isPure()) {
                return data;
            } else {
                return processAssignment(node.getOperand(), node, true, data);
            }
        }

        @Override
        public SpanInfo visit(ASTAssignmentExpression node, SpanInfo data) {
            // visit operands in order
            data = acceptOpt(node.getRightOperand(), data);
            data = acceptOpt(node.getLeftOperand(), data);

            return processAssignment(node.getLeftOperand(),
                node.getRightOperand(),
                node.getOperator().isCompound(),
                data);

        }

        private SpanInfo processAssignment(ASTExpression lhs0, // LHS or unary operand
                                           ASTExpression rhs,  // RHS or unary
                                           boolean useBeforeAssigning,
                                           SpanInfo result) {

            if (lhs0 instanceof ASTNamedReferenceExpr) {
                ASTNamedReferenceExpr lhs = (ASTNamedReferenceExpr) lhs0;
                JVariableSymbol lhsVar = lhs.getReferencedSym();
                if (lhsVar != null && (lhsVar instanceof JLocalVariableSymbol || isRelevantField(lhs))) {

                    VarLocalInfo oldVar = result.assign(lhsVar, rhs);
                    SpanInfo.updateReachingDefs(lhs, lhsVar, oldVar);
                }
            }
            return result;
        }

        private boolean isRelevantField(ASTExpression lhs) {
            return lhs instanceof ASTNamedReferenceExpr && (trackThisInstance() && JavaAstUtils.isThisFieldAccess(lhs)
                                                            || isStaticFieldOfThisClass(((ASTNamedReferenceExpr) lhs).getReferencedSym()));
        }

        private boolean isStaticFieldOfThisClass(JVariableSymbol var) {
            return var instanceof JFieldSymbol
                   && ((JFieldSymbol) var).isStatic()
                   && enclosingClassScope.equals(((JFieldSymbol) var).getEnclosingClass());
        }

        private static JVariableSymbol getVarIfUnaryAssignment(ASTUnaryExpression node) {
            ASTExpression operand = node.getOperand();
            if (!node.getOperator().isPure() && operand instanceof ASTNamedReferenceExpr) {
                return ((ASTNamedReferenceExpr) operand).getReferencedSym();
            }
            return null;
        }

        // variable usage

        @Override
        public SpanInfo visit(ASTVariableAccess node, SpanInfo data) {
            if (node.getAccessType() == AccessType.READ) {
                data.use(node.getReferencedSym(), node);
            }
            return data;
        }

        @Override
        public SpanInfo visit(ASTFieldAccess node, SpanInfo data) {
            data = node.getQualifier().acceptVisitor(this, data);
            if (node.getAccessType() == AccessType.READ) {
                data.use(node.getReferencedSym(), node);
            }
            return data;
        }

        @Override
        public SpanInfo visit(ASTThisExpression node, SpanInfo data) {
            if (trackThisInstance() && isThisExprLeaking(node)) {
                data.recordThisLeak(enclosingClassScope, node);
            }
            return data;
        }

        private static boolean isThisExprLeaking(ASTThisExpression node) {
            boolean isAllowed = node.getParent() instanceof ASTFieldAccess
                                || node.getParent() instanceof ASTSynchronizedStatement;
            return !isAllowed;
        }

        @Override
        public SpanInfo visit(ASTMethodCall node, SpanInfo state) {
            if (trackThisInstance() && JavaAstUtils.isCallOnThisInstance(node) != OptionalBool.NO) {
                state.recordThisLeak(enclosingClassScope, node);
            }
            return visitInvocationExpr(node, state);
        }

        @Override
        public SpanInfo visit(ASTConstructorCall node, SpanInfo state) {
            state = visitInvocationExpr(node, state);
            acceptOpt(node.getAnonymousClassDeclaration(), state);
            return state;
        }

        @Override
        public SpanInfo visit(ASTArrayAllocation node, SpanInfo state) {
            state = acceptOpt(node.getArrayInitializer(), state);
            state = acceptOpt(node.getTypeNode().getDimensions(), state);
            // May throw OOM error for instance. This abrupt completion routine is
            // noop if we are outside a try block.
            state.abruptCompletionByThrow(true);
            return state;
        }

        private <T extends InvocationNode & QualifiableExpression> SpanInfo visitInvocationExpr(T node, SpanInfo state) {
            state = acceptOpt(node.getQualifier(), state);
            state = acceptOpt(node.getArguments(), state);

            // todo In 7.0, with the precise type/overload resolution, we
            //  could only target methods that throw checked exceptions
            //  (unless some catch block catches an unchecked exceptions)

            state.abruptCompletionByThrow(true); // this is a noop if we're outside a try block that has catch/finally
            return state;
        }


        // ctor/initializer handling

        @Override
        public SpanInfo visitTypeDecl(ASTTypeDeclaration node, SpanInfo data) {
            return processTypeDecl(node, data);
        }

        private static SpanInfo processTypeDecl(ASTTypeDeclaration node, SpanInfo data) {
            ForwardDataflowVisitor instanceVisitor = new ForwardDataflowVisitor(node.getSymbol(), false);
            ForwardDataflowVisitor staticVisitor = new ForwardDataflowVisitor(node.getSymbol(), true);
            // process initializers and ctors first
            processInitializers(node.getDeclarations(), data, node.getSymbol(),
                instanceVisitor, staticVisitor);

            for (ASTBodyDeclaration decl : node.getDeclarations()) {
                if (decl instanceof ASTMethodDeclaration) {
                    ASTMethodDeclaration method = (ASTMethodDeclaration) decl;
                    if (method.getBody() != null) {
                        SpanInfo span = data.forkCapturingNonLocal();
                        boolean staticCtx = method.isStatic();
                        span.declareSpecialFieldValues(node.getSymbol(), staticCtx);
                        SpanInfo endState;
                        if (staticCtx) {
                            endState = staticVisitor.acceptOpt(decl, span);
                        } else {
                            endState = instanceVisitor.acceptOpt(decl, span);
                        }
                        useAllSelfFields(endState, staticCtx, node.getSymbol());
                    }
                } else if (decl instanceof ASTTypeDeclaration) {
                    processTypeDecl((ASTTypeDeclaration) decl, data.forkEmptyNonLocal());
                }
            }
            return data;
        }

        private static void processInitializers(NodeStream<ASTBodyDeclaration> declarations,
                                                SpanInfo beforeLocal,
                                                @NonNull JClassSymbol classSymbol,
                                                ForwardDataflowVisitor instanceVisitor,
                                                ForwardDataflowVisitor staticVisitor) {

            // All static field initializers + static initializers
            SpanInfo staticInit = beforeLocal.forkEmptyNonLocal();

            List<ASTBodyDeclaration> ctors = new ArrayList<>();
            // Those are initializer blocks and instance field initializers
            List<ASTBodyDeclaration> ctorHeaders = new ArrayList<>();

            for (ASTBodyDeclaration declaration : declarations) {
                final boolean isStatic;
                if (declaration instanceof ASTEnumConstant) {
                    isStatic = true;
                } else if (declaration instanceof ASTFieldDeclaration) {
                    isStatic = ((ASTFieldDeclaration) declaration).isStatic();
                } else if (declaration instanceof ASTInitializer) {
                    isStatic = ((ASTInitializer) declaration).isStatic();
                } else if (declaration instanceof ASTConstructorDeclaration
                           || declaration instanceof ASTCompactConstructorDeclaration) {
                    ctors.add(declaration);
                    continue;
                } else {
                    continue;
                }

                if (isStatic) {
                    staticInit = staticVisitor.acceptOpt(declaration, staticInit);
                } else {
                    ctorHeaders.add(declaration);
                }
            }

            // Static init is done, mark all static fields as escaping
            useAllSelfFields(staticInit, true, classSymbol);


            // All field initializers + instance initializers
            // This also contains the static definitions, as the class must be
            // initialized before an instance is created.
            SpanInfo ctorHeader = beforeLocal.forkCapturingNonLocal().absorb(staticInit);

            // Static fields get an "initial value" placeholder before starting instance ctors
            ctorHeader.declareSpecialFieldValues(classSymbol, true);

            for (ASTBodyDeclaration fieldInit : ctorHeaders) {
                ctorHeader = instanceVisitor.acceptOpt(fieldInit, ctorHeader);
            }

            SpanInfo ctorEndState = ctors.isEmpty() ? ctorHeader : null;
            for (ASTBodyDeclaration ctor : ctors) {
                SpanInfo ctorBody = ctorHeader.forkCapturingNonLocal();
                ctorBody.declareSpecialFieldValues(classSymbol, true);
                SpanInfo state = instanceVisitor.acceptOpt(ctor, ctorBody);
                ctorEndState = ctorEndState == null ? state : ctorEndState.absorb(state);
            }

            // assignments that reach the end of any constructor must be considered used
            useAllSelfFields(ctorEndState, false, classSymbol);
        }

        static void useAllSelfFields(SpanInfo state, boolean inStaticCtx, JClassSymbol enclosingSym) {
            for (JFieldSymbol field : enclosingSym.getDeclaredFields()) {
                if (!inStaticCtx || field.isStatic()) {
                    JavaNode escapingNode = enclosingSym.tryGetNode();
                    state.assignOutOfScope(field, escapingNode, SpecialAssignmentKind.INITIAL_FIELD_VALUE);
                }
            }
        }
    }

    /**
     * The shared state for all {@link SpanInfo} instances in the same
     * toplevel class.
     */
    private static final class GlobalAlgoState {

        final TargetStack breakTargets = new TargetStack();
        // continue jumps to the condition check, while break jumps to after the loop
        final TargetStack continueTargets = new TargetStack();

        private GlobalAlgoState(Set<ValueFacts> allAssignments,
                                Set<ValueFacts> usedAssignments,
                                Map<ValueFacts, Set<ValueFacts>> killRecord) {

        }

        private GlobalAlgoState() {
            this(new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new LinkedHashMap<>());
        }
    }

    // Information about a variable in a code span.
    static class VarLocalInfo {

        // this is not modified so can be shared between different SpanInfos.
        final Set<ValueFacts> reachingDefs;

        VarLocalInfo(Set<ValueFacts> reachingDefs) {
            this.reachingDefs = reachingDefs;
        }

        // and produce an independent instance
        VarLocalInfo merge(VarLocalInfo other) {
            if (other == this) { // NOPMD #3205
                return this;
            }
            Set<ValueFacts> merged = new LinkedHashSet<>(reachingDefs.size() + other.reachingDefs.size());
            merged.addAll(reachingDefs);
            merged.addAll(other.reachingDefs);
            return new VarLocalInfo(merged);
        }

        @Override
        public String toString() {
            return "VarLocalInfo{reachingDefs=" + reachingDefs + '}';
        }

    }

    /**
     * Information about a span of code.
     */
    private static final class SpanInfo {

        // spans are arranged in a tree, to look for enclosing finallies
        // when abrupt completion occurs. Blocks that have non-local
        // control-flow (lambda bodies, anonymous classes, etc) aren't
        // linked to the outer parents.
        final SpanInfo parent;

        // If != null, then abrupt completion in this span of code (and any descendant)
        // needs to go through the finally span (the finally must absorb it)
        SpanInfo myFinally = null;


        /**
         * Inside a try block, we assume that any method/ctor call may
         * throw, which means, any assignment reaching such a method call
         * may reach the catch blocks if there are any.
         */
        List<SpanInfo> myCatches;

        final GlobalAlgoState global;

        final Map<JVariableSymbol, VarLocalInfo> symtable;

        /**
         * Whether the current span completed abruptly. Abrupt
         * completion occurs with break, continue, return or throw
         * statements. A loop whose body completes abruptly may or
         * may not complete abruptly itself. For instance in
         * <pre>{@code
         * for (int i = 0; i < 5; i++) {
         *     break;
         * }
         * }</pre>
         * the loop body completes abruptly on all paths, but the loop
         * itself completes normally. This is also the case in a switch
         * statement where all cases are followed by a break.
         */
        private OptionalBool hasCompletedAbruptly = OptionalBool.NO;

        /**
         * Collects the abrupt completion targets of the current span.
         * The value {@link #returnOrThrowTarget}
         * represents a return statement or a throw that
         * is not followed by an enclosing finally block.
         */
        private PSet<SpanInfo> abruptCompletionTargets = HashTreePSet.empty();

        /**
         * Sentinel to represent the target of a throw or return statement.
         */
        private final SpanInfo returnOrThrowTarget;


        private SpanInfo(GlobalAlgoState global) {
            this(null, global, new LinkedHashMap<>());
        }

        private SpanInfo(@Nullable SpanInfo parent,
                         GlobalAlgoState global,
                         Map<JVariableSymbol, VarLocalInfo> symtable) {
            this.parent = parent;
            this.returnOrThrowTarget = parent == null ? this : parent.returnOrThrowTarget;
            this.global = global;
            this.symtable = symtable;
            this.myCatches = Collections.emptyList();
        }

        boolean hasVar(ASTVariableId var) {
            return symtable.containsKey(var.getSymbol());
        }

        void deleteVar(JVariableSymbol var) {
            symtable.remove(var);
        }

        /**
         * Record a leak of the `this` reference in a ctor (including field initializers).
         *
         * <p>This means, all defs reaching this point, for all fields
         * of `this`, may be used in the expression. We assume that the
         * ctor finishes its execution atomically, that is, following
         * definitions are not observable at an arbitrary point (that
         * would be too conservative).
         *
         * <p>Constructs that are considered to leak the `this` reference
         * (only processed if they occur in a ctor):
         * - using `this` as a method/ctor argument
         * - using `this` as the receiver of a method/ctor invocation (also implicitly)
         *
         * <p>Because `this` may be aliased (eg in a field, a local var,
         * inside an anon class or capturing lambda, etc), any method
         * call, on any receiver, may actually observe field definitions
         * of `this`. So the analysis may show some false positives, which
         * hopefully should be rare enough.
         */
        public void recordThisLeak(JClassSymbol enclosingClassSym, JavaNode escapingNode) {
            // all reaching defs to fields until now may be observed
            for (JFieldSymbol field : enclosingClassSym.getDeclaredFields()) {
                if (!field.isStatic()) {
                    assignOutOfScope(field, escapingNode, SpecialAssignmentKind.UNKNOWN_METHOD_CALL);
                }
            }
        }

        void declareSpecialFieldValues(JClassSymbol sym, boolean onlyStatic) {
            List<JFieldSymbol> declaredFields = sym.getDeclaredFields();
            for (JFieldSymbol field : declaredFields) {
                if (onlyStatic && !field.isStatic()) {
                    continue;
                }
                ASTVariableId id = field.tryGetNode();
                if (id == null) {
                    continue;
                }

                assign(field, id, DataflowPass.SpecialAssignmentKind.INITIAL_FIELD_VALUE);
            }
        }

        public void assign(JVariableSymbol formal, JavaNode varId) {

        }

        // Forks duplicate this context, to preserve the reaching defs
        // of the current context while analysing a sub-block
        // Forks must be merged later if control flow merges again, see ::absorb

        SpanInfo fork() {
            return doFork(this, copyTable());
        }

        SpanInfo forkEmpty() {
            return doFork(this, new LinkedHashMap<>());
        }


        SpanInfo forkEmptyNonLocal() {
            return doFork(null, new LinkedHashMap<>());
        }

        SpanInfo forkCapturingNonLocal() {
            return doFork(null, copyTable());
        }

        private Map<JVariableSymbol, VarLocalInfo> copyTable() {
            return new LinkedHashMap<>(this.symtable);
        }

        private SpanInfo doFork(/*nullable*/ SpanInfo parent, Map<JVariableSymbol, VarLocalInfo> reaching) {
            return new SpanInfo(parent, this.global, reaching);
        }

        /** Abrupt completion for return, continue, break. */
        SpanInfo abruptCompletion(@NonNull SpanInfo target) {
            hasCompletedAbruptly = OptionalBool.YES;
            abruptCompletionTargets = abruptCompletionTargets.plus(target);

            SpanInfo parent = this;
            while (parent != null) {
                if (parent.myFinally != null) {
                    parent.myFinally.absorb(this);
                    // stop on the first finally, its own end state will
                    // be merged into the nearest enclosing finally
                    break;
                }
                if (parent == target) { // NOPMD CompareObjectsWithEqual this is what we want
                    break;
                }
                parent = parent.parent;

            }

            // rest of this block is dead code so we don't track declarations
            this.symtable.clear();
            return this;
        }


        /**
         * Record an abrupt completion occurring because of a thrown
         * exception.
         *
         * @param byMethodCall If true, a method/ctor call threw the exception
         *                     (we conservatively consider they do inside try blocks).
         *                     Otherwise, a throw statement threw.
         */
        SpanInfo abruptCompletionByThrow(boolean byMethodCall) {
            // Find the first block that has a finally
            // Be absorbed into every catch block on the way.

            // todo In 7.0, with the precise type/overload resolution, we
            // can target the specific catch block that would catch the
            // exception.
            if (!byMethodCall) {
                hasCompletedAbruptly = OptionalBool.YES;
            }
            abruptCompletionTargets = abruptCompletionTargets.plus(returnOrThrowTarget);

            SpanInfo parent = this;
            while (parent != null) {

                if (!parent.myCatches.isEmpty()) {
                    for (SpanInfo c : parent.myCatches) {
                        c.absorb(this);
                    }
                }

                if (parent.myFinally != null) {
                    // stop on the first finally, its own end state will
                    // be merged into the nearest enclosing finally
                    parent.myFinally.absorb(this);
                    return this;
                }
                parent = parent.parent;
            }


            if (!byMethodCall) {
                this.symtable.clear(); // following is dead code
            }
            return this;
        }

        SpanInfo withCatchBlocks(List<SpanInfo> catchStmts) {
            assert myCatches.isEmpty() || catchStmts.isEmpty() : "Cannot set catch blocks twice";
            myCatches = Collections.unmodifiableList(catchStmts); // we own the list now, to avoid copying
            return this;
        }

        SpanInfo absorb(SpanInfo other) {
            // Merge reaching defs of the other scope into this
            // This is used to join paths after the control flow has forked

            // a spanInfo may be absorbed several times so this method should not
            // destroy the parameter
            if (other == this || other == null || other.symtable.isEmpty()) { // NOPMD #3205
                return this;
            }

            CollectionUtil.mergeMaps(this.symtable, other.symtable, VarLocalInfo::merge);
            this.hasCompletedAbruptly = mergeCertitude(this.hasCompletedAbruptly, other.hasCompletedAbruptly);
            this.abruptCompletionTargets = CollectionUtil.union(this.abruptCompletionTargets, other.abruptCompletionTargets);
            return this;
        }

        static OptionalBool mergeCertitude(OptionalBool first, OptionalBool other) {
            return first == other ? first : OptionalBool.UNKNOWN;
        }


        @Override
        public String toString() {
            return symtable.toString();
        }

    }

    static class TargetStack {

        final Deque<SpanInfo> unnamedTargets = new ArrayDeque<>();
        final Map<String, SpanInfo> namedTargets = new LinkedHashMap<>();


        void push(SpanInfo state) {
            unnamedTargets.push(state);
        }

        SpanInfo pop() {
            return unnamedTargets.pop();
        }

        SpanInfo peek() {
            return unnamedTargets.getFirst();
        }

        SpanInfo doBreak(SpanInfo data, /* nullable */ String label) {
            // basically, reaching defs at the point of the break
            // also reach after the break (wherever it lands)
            SpanInfo target;
            if (label == null) {
                target = unnamedTargets.getFirst();
            } else {
                target = namedTargets.get(label);
            }

            if (target != null) { // otherwise CT error
                target.absorb(data);
                return data.abruptCompletion(target);
            }
            return data;
        }
    }

    /**
     * Represents facts we know about a trackable "location" (variable, field, array component)
     * at a given program point.
     */
    private static class ValueFacts {
        final @Nullable JVariableSymbol var;
        // this is not necessarily an expression, it may be also the
        // variable declarator of a foreach loop or a variable id
        final JavaNode node;
        final Map<DfAnalysis<?>, Object> analysisToState = new HashMap<>();

        ValueFacts(JVariableSymbol var, JavaNode node) {
            this.var = var;
            this.node = node;
        }

        public JavaNode getLocation() {
            return node;
        }

        void absorb(ValueFacts facts) {
            for (Map.Entry<DfAnalysis<?>, Object> entry : analysisToState.entrySet()) {
                DfAnalysis<?> analysis = entry.getKey();
                Object other = facts.analysisToState.get(analysis);
                if (other == null) {
                    continue;
                }
                Object x = joinCapture(analysis, entry.getValue(), other);
                entry.setValue(x);
            }
        }

        private <T extends ValueModel<T>> T joinCapture(DfAnalysis<T> analysis, Object a, Object b) {
            @SuppressWarnings("unchecked")
            T aAsT = (T) a;
            @SuppressWarnings("unchecked")
            T bAsT = (T) b;
            return aAsT.join(bAsT);
        }
    }
}
