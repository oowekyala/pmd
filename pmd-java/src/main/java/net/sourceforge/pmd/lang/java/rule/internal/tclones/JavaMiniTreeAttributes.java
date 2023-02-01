/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.Objects;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArrayType;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTClassLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.ast.ASTFieldAccess;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTImplementsList;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeArguments;
import net.sourceforge.pmd.lang.java.ast.ASTVariableAccess;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AccessNode.Visibility;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.rule.internal.tclones.MiniTree.MiniTreeBuilder;

/**
 *
 */
public final class JavaMiniTreeAttributes {


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

    private static final class AttributesHasher extends JavaVisitorBase<MiniTreeBuilder, Void> {
        // note that nodes of a given type need to add exactly the same attributes in the same order

        static final AttributesHasher INSTANCE = new AttributesHasher();

        @Override
        protected Void visitChildren(Node node, MiniTreeBuilder data) {
            return null; // don't recurse
        }


        @Override
        public Void visit(ASTMethodCall node, MiniTreeBuilder data) {
            // method names are used in the structure filter too.
            data.hashAttr("name", node.getMethodName());
            return null;
        }


        @Override
        public Void visit(ASTFieldAccess node, MiniTreeBuilder data) {
            data.hashAttr("name", node.getName());
            return null;
        }


        @Override
        public Void visit(ASTVariableAccess node, MiniTreeBuilder data) {
            if (node.getReferencedSym() != null && node.getReferencedSym().isField()) {
                data.hashAttr("name", node.getName());
            }
            return null;
        }


        @Override
        public Void visit(ASTVariableDeclaratorId node, MiniTreeBuilder data) {
            if (node.getVisibility() == Visibility.V_LOCAL) {
                // local vars can be renamed so they don't count.
                return null;
            }
            // field names eg are structural.
            data.hashAttr("name", node.getName());
            return null;
        }


        @Override
        public Void visitLiteral(ASTLiteral node, MiniTreeBuilder data) {
            // We store only the hashcode so as not to keep strings in memory
            // Also the value is not part of the structural hash
            data.addAttrWithoutHash("value", Objects.hashCode(node.getConstValue()));
            return null;
        }


        @Override
        public Void visit(ASTInfixExpression node, MiniTreeBuilder data) {
            data.perfectHashAttr("op", node.getOperator());
            return null;
        }


        @Override
        public Void visit(ASTAssignmentExpression node, MiniTreeBuilder data) {
            data.perfectHashAttr("op", node.getOperator());
            return null;
        }


        @Override
        public Void visitType(ASTType node, MiniTreeBuilder data) {
            if (node.getParent() instanceof ASTTypeArguments
                || node.getParent() instanceof ASTType) {
                return null;
            }
            if (isTypeStructural(node)) {
                if (node instanceof ASTPrimitiveType) {
                    data.perfectHashAttr("t", ((ASTPrimitiveType) node).getKind());
                } else {
                    data.hashAttr("t", node.getTypeMirror().toString());
                }
            } else {
                data.addAttrWithoutHash("t", node.getTypeMirror().toString());
            }
            return null;
        }


        private static boolean isTypeStructural(ASTType node) {
            JavaNode parent = node.getParent();
            return parent instanceof ASTFieldDeclaration
                || parent instanceof ASTExtendsList
                || parent instanceof ASTImplementsList
                || true;
        }


        @Override
        public Void visit(ASTMethodDeclaration node, MiniTreeBuilder data) {
            // the name only somewhat matters when we're overriding
            // something, because it's not declared locally
            // todo this should be generalized with the external ref check
            //  other things to consider in the generalization:
            //  - remove super., this. and static qualifiers
            String name = node.isOverridden() ? node.getName() : null;
            data.hashAttr("overriddenName", name);
            return null;
        }

        // we do not hash variable names (in declarator id or field/var access)
        // this means trees identical modulo renaming of those vars will
        // be placed in the same bucket
    }

    private JavaMiniTreeAttributes() {
        // utility class
    }
}
