/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.impl.GenericNode;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 *
 */
public class MiniTreeBuilderVisitor {


    private static final MiniAstHandler<JavaNode> JAVA_CONFIG = new MiniAstHandler<JavaNode>() {
        @Override
        public void hashAttributes(JavaNode node, MiniTreeBuilder builder) {
            node.acceptVisitor(AttributesHasher.INSTANCE, builder);
        }

        @Override
        public int getRuleKind(JavaNode node) {
            return node.getProductionId();
        }
    };

    interface MiniAstHandler<N extends GenericNode<N>> {

        void hashAttributes(N node, MiniTreeBuilder builder);

        default int getRuleKind(N node) {
            return node.getClass().hashCode();
        }
    }

    interface CloneDetectorGlobals {

        void acceptTree(MiniTree tree);

    }

    static MiniTree buildJavaMiniTree(JavaNode root, CloneDetectorGlobals sink) {
        return buildMiniTree(root, new MiniTreeBuilder(), JAVA_CONFIG, sink);
    }


    static <N extends GenericNode<N>> MiniTree buildMiniTree(N root, MiniAstHandler<N> config, CloneDetectorGlobals sink) {
        return buildMiniTree(root, new MiniTreeBuilder(), config, sink);
    }

    private static <N extends GenericNode<N>> MiniTree buildMiniTree(N node,
                                                                     MiniTreeBuilder myBuilder,
                                                                     MiniAstHandler<N> config,
                                                                     CloneDetectorGlobals globals) {
        myBuilder.hashKind(config.getRuleKind(node));

        MiniTreeBuilder childrenBuilder = new MiniTreeBuilder();

        // builder may be reset and reused for all children
        for (N child : node.children()) {
            MiniTree childTree = buildMiniTree(child, childrenBuilder, config, globals);
            myBuilder.addChild(childTree);
        }

        config.hashAttributes(node, myBuilder);

        MiniTree built = myBuilder.buildAndReset();
        globals.acceptTree(built);
        return built;
    }


    static final class AttributesHasher extends JavaVisitorBase<MiniTreeBuilder, Void> {

        static final AttributesHasher INSTANCE = new AttributesHasher();

        @Override
        protected Void visitChildren(Node node, MiniTreeBuilder data) {
            return null; // don't recurse
        }

        @Override
        public Void visitLiteral(ASTLiteral node, MiniTreeBuilder data) {
            data.hashInt("value", node.getFirstToken().getImageCs().hashCode());
            return null;
        }

        @Override
        public Void visit(ASTMethodCall node, MiniTreeBuilder data) {
            data.hashAttr("name", node.getMethodName());
            return null;
        }

        @Override
        public Void visit(ASTVariableAccess node, MiniTreeBuilder data) {
            data.hashAttr("name", node.getName());
            return null;
        }

        @Override
        public Void visit(ASTFieldAccess node, MiniTreeBuilder data) {
            data.hashAttr("name", node.getName());
            return null;
        }
    }
}
