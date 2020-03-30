/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast.internal;

import java.util.ArrayDeque;
import java.util.Deque;

import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;

/**
 * A special visitor, that uses recursion very scarcely. This makes the
 * call stack not grow very deep even on huge trees. This feature was
 * added in PMD 7 to cope with very deep infix expression trees, which
 * can be found in generated code (eg a string concatenation with 4000
 * operators).
 *
 * TODO this is only used by internal analysis passes for now but rules that
 *   don't use the rulechain may have the same problem. Using this in rules
 *   also has a huge advantage: since jjtAccept is not recursive anymore,
 *   we can surround it with a try-catch and report exceptions on the
 *   *node* on which they occurred, which is much more helpful that just
 *   the file.
 *
 * <p>TreeProcessors need to be applied only with {@link #processTree(TreeProcessor, JavaNode)}.
 *
 * <p>Implementation needs a bit of care, see {@link #visitSubtree(JavaNode)}.
 *
 * <p>Principle: nodes to be process are stored in a queue. If an overridden
 * visit method needs to do some setup/cleanup when entering and exiting
 * a node, it needs to call {@link #visitSubtree(JavaNode)}. This will setup
 * a new queue and process the subtree entirely. If the node is not
 * interesting to the processor (ie, the visit method is not overridden),
 * the children of that node will not be processed through recursion,
 * but by adding them to the queue. This means in this case, the call
 * stack does not grow.
 */
public abstract class TreeProcessor extends JavaParserVisitorAdapter {

    /**
     * Visit all the descendants of the given node (excluding itself).
     * When this method exits, the whole subtree will have been visited.
     * This can be used to implement setup/cleanup behaviour in the
     * overridden visit methods. Here's a template:
     *
     * <pre>{@code
     *   @Override
     *   public Object visit(ASTAnyTypeDeclaration node, Object data) {
     *      // setup some context that will be available
     *      // during the visit of the descendants
     *
     *      visitSubtree(node);
     *
     *      // cleanup the context
     *   }
     * }</pre>
     *
     * <p>Note that calling {@code super.visit(node, data);} here would
     * not process the subtree before the cleanup tasks, but just ask that
     * the subtree be processed later.
     *
     *
     * <pre>{@code
     *   @Override
     *   public Object visit(ASTAnyTypeDeclaration node, Object data) {
     *      // do something or check something
     *      // that doesn't need cleanup
     *      super.visit(node, data);
     *   }
     * }</pre>
     * This is ok, because the {@code super} call occurs as a tail call.
     * Note that it is necessary to pass the {@code data} parameter around
     * unchanged.
     */
    protected final void visitSubtree(JavaNode node) {
        TreeWalker walker = new TreeWalker();
        walker.enqueueChildren(node);
        walker.walk(this);
    }

    /**
     * This method is delegated to by every visit method unless they are
     * overridden. It will ask that every child is processed in order,
     * but the processing occurs later.
     */
    @Override
    public final Object visit(JavaNode node, Object data) {
        if (!(data instanceof TreeWalker)) {
            throw new IllegalArgumentException(
                data + " was passed to TreeProcessor::visit, the initial argument should be passed without being touched."
            );
        }
        // don't recurse
        ((TreeWalker) data).enqueueChildren(node);
        return null;
    }

    /**
     * Apply the given processor to the tree.
     *
     * @param processor Processor
     * @param root      Root node of the tree, the first to be processed
     */
    public static void processTree(TreeProcessor processor, JavaNode root) {
        TreeWalker walker = new TreeWalker();
        walker.todo.add(root);
        walker.walk(processor);
    }

    private static final class TreeWalker {


        private final Deque<JavaNode> todo = new ArrayDeque<>();

        void enqueueChildren(JavaNode node) {
            for (int i = node.getNumChildren() - 1; i >= 0; i--) {
                todo.addFirst(node.getChild(i));
            }
        }

        void walk(TreeProcessor processor) {
            JavaNode node;
            while ((node = todo.pollFirst()) != null) {
                node.jjtAccept(processor, this);
            }
        }
    }
}
