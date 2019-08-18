package net.sourceforge.pmd.lang.java.ast;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TreeWalkUtils {

    public static <T> void postOrderWalk(JavaNode node, T data, BiConsumer<JavaNode, T> visitor) {

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            postOrderWalk(node.jjtGetChild(i), data, visitor);
        }

        visitor.accept(node, data);
    }

    public static void postOrderWalk(JavaNode node, Consumer<JavaNode> consumer) {

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            postOrderWalk(node.jjtGetChild(i), consumer);
        }

        consumer.accept(node);
    }

    public static void preOrderWalk(JavaNode node, Consumer<JavaNode> consumer) {

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            postOrderWalk(node.jjtGetChild(i), consumer);
        }

        consumer.accept(node);
    }

    public static <T> void postOrderWalk(JavaNode node, SideEffectingVisitor<T> visitor, T data) {
        postOrderWalk(node, data, (n, d) -> n.jjtAccept(visitor, d));
    }

}
