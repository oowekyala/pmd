/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * A node test. A node test is a condition on the name, {@linkplain XmdNodeKind kind},
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
     * KindTest ::= {@linkplain ASTDocumentTest DocumentTest}
     *            | {@linkplain ASTElementTest ElementTest}
     *            | {@linkplain ASTAttributeTest AttributeTest}
     *            | {@linkplain ASTSchemaElementTest SchemaElementTest}
     *            | {@linkplain ASTSchemaAttributeTest SchemaAttributeTest}
     *            | {@linkplain ASTProcessingInstructionTest ProcessingInstructionTest}
     *            | {@linkplain ASTCommentTest CommentTest}
     *            | {@linkplain ASTTextTest TextTest}
     *            | {@linkplain ASTNamespaceNodeTest NamespaceNodeTest}
     *            | {@linkplain ASTAnyKindTest AnyKindTest}
     *
     * </pre>
     */
    interface KindTest extends NodeTest, ItemType {

        /**
         * Groups {@linkplain ASTSchemaElementTest SchemaElementTest}
         * and {@linkplain ASTElementTest ElementTest} under a common interface.
         */
        interface ElementTestOrSchemaElementTest extends KindTest {

        }

    }

    /**
     * A name test on a node.
     *
     * <pre>
     *
     * NameTest ::= {@linkplain ASTExactNameTest ExactNameTest}
     *            | {@linkplain ASTWildcardNameTest WildcardNameTest}
     *
     * </pre>
     */
    interface NameTest extends NodeTest {

    }
}
