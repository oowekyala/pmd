/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * A node test. A node test is a condition on the name, {@link XmdNodeKind kind},
 * and/or type annotation of a node. A node test determines which nodes
 * contained by an axis are selected by a step.
 *
 * <pre>
 *
 * (: Both the productions are not nodes. :)
 * NodeTest ::= {@link NameTest} | {@link KindTest}
 *
 * </pre>
 */
public interface NodeTest extends XPathNode {


    /**
     * A kind test on a node. This is a NodeTest and an ItemType.
     *
     * <pre>
     *
     * KindTest ::= {@link ASTDocumentTest DocumentTest}
     *            | {@link ASTElementTest ElementTest}
     *            | {@link ASTAttributeTest AttributeTest}
     *            | {@link ASTSchemaElementTest SchemaElementTest}
     *            | {@link ASTSchemaAttributeTest SchemaAttributeTest}
     *            | {@link ASTProcessingInstructionTest ProcessingInstructionTest}
     *            | {@link ASTCommentTest CommentTest}
     *            | {@link ASTTextTest TextTest}
     *            | {@link ASTNamespaceNodeTest NamespaceNodeTest}
     *            | {@link ASTAnyKindTest AnyKindTest}
     *
     * </pre>
     */
    interface KindTest extends NodeTest, ItemType {

        /**
         * Groups {@link ASTSchemaElementTest SchemaElementTest}
         * and {@link ASTElementTest ElementTest} under a common interface.
         */
        interface ElementTestOrSchemaElementTest extends KindTest {

        }

    }

    /**
     * A name test on a node.
     *
     * <pre>
     *
     * NameTest ::= {@link ASTExactNameTest ExactNameTest}
     *            | {@link ASTWildcardNameTest WildcardNameTest}
     *
     * </pre>
     */
    interface NameTest extends NodeTest {

    }
}
