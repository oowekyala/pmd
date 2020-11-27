/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 *
 */
public class JavaMiniTreeAttributes {


    static final MiniAstHandler<JavaNode> JAVA_CONFIG = new MiniAstHandler<JavaNode>() {

        @Override
        public boolean isIgnored(JavaNode node) {
            return node instanceof ASTImportDeclaration
                || node instanceof ASTPackageDeclaration;
        }

        @Override
        public boolean isSequencer(JavaNode node) {
            return node instanceof ASTBlock;
        }

        @Override
        public void hashAttributes(JavaNode node, MiniTreeBuilder builder) {
            node.acceptVisitor(AttributesHasher.INSTANCE, builder);
        }

        @Override
        public int getRuleKind(JavaNode node) {
            return node.getProductionId();
        }
    };

    static final class AttributesHasher extends JavaVisitorBase<MiniTreeBuilder, Void> {

        static final AttributesHasher INSTANCE = new AttributesHasher();

        @Override
        protected Void visitChildren(Node node, MiniTreeBuilder data) {
            return null; // don't recurse
        }

        @Override
        public Void visit(ASTMethodCall node, MiniTreeBuilder data) {
            data.hashAttr("name", node.getMethodName().intern());
            return null;
        }

        @Override
        public Void visitLiteral(ASTLiteral node, MiniTreeBuilder data) {
            data.addAttrWithoutHash("value", node.getConstValue());
            return null;
        }

        @Override
        public Void visit(ASTInfixExpression node, MiniTreeBuilder data) {
            data.hashAttr("op", node.getOperator());
            return null;
        }

        @Override
        public Void visit(ASTPrimitiveType node, MiniTreeBuilder data) {
            data.hashAttr("k", node.getKind());
            return null;
        }

        @Override
        public Void visit(ASTMethodDeclaration node, MiniTreeBuilder data) {
            if (node.isOverridden()) {
                // the name only somewhat matters when we're overriding something, because it's not declared locally
                // todo this should be generalized with the external ref check
                //  other things to consider in the generalization:
                //  - remove super., this. and static qualifiers
                data.hashAttr("overriddenName", node.getName());
            }
            return null;
        }

        // we do not hash variable names (in declarator id or field/var access)
        // this means trees identical modulo renaming of those vars will
        // be placed in the same bucket
    }
}
