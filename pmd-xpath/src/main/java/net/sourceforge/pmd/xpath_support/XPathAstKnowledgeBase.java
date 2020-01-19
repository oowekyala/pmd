/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.xpath_support;

import java.util.Optional;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Metadata about the structure of the AST for
 * a specific language. This is issued by a Language
 * instance to this framework to preprocess XPath
 * queries.
 *
 * @since 6.10.0
 */
public interface XPathAstKnowledgeBase {

    // Mustn't use any of the classes of the XPath AST, since it will
    // be provided by other language implementations.


    /**
     * Returns the URI identifying the namespace
     * of the nodes of this language. This is an
     * XML namespace URI, so may actually be anything,
     * provided it's different from the namespaces of
     * other languages.
     */
    String getLanguageNamespaceUri();


    Set<XPathFunctionMetadata> getRegisteredFunctions();


    /**
     * Information about a registered XPath function.
     */
    interface XPathFunctionMetadata {

        /**
         * Returns the name of the function.
         *
         * <p>A function is selected with just
         * its name and arity. Argument types
         * are just used for type checking
         */
        String getFunctionName();


        /**
         * Returns the arity of the function, i.e.
         * number of expected parameters.
         */
        int getArity();

        // TODO add dependencies

    }


    /**
     * Information about a node accessible from XPath.
     *
     * <p>XPath node names are decoupled from this implementation.
     *
     * Accessible means their name addresses a set of
     * concrete node types in the context of an XPath
     * query on their language. As such, we may make
     * some interface names shortcuts for a union of
     * concrete node names, eg "AnyTypeDeclaration" would be
     * reduced to "(ClassDeclaration | InterfaceDeclaration)".
     */
    interface XPathAccessibleNodeMetadata {

        /**
         * Name with which the node is referred to in
         * an XPath query. The namespace is taken to be
         * that of the language.
         */
        String getXPathNodeName();


        /**
         * Abstract common type for the given nodes.
         */
        Class<? extends Node> getType();


        Set<XPathAttributeMetadata> getValidAttributes();


        /**
         * Returns the set of concrete node types this
         * node may have at runtime.
         */
        Set<Class<? extends Node>> getConcreteTypeSet();

        // TODO add dependencies
    }

    interface XPathAttributeMetadata {

        String getXPathAttributeName();


        Optional<AttributeDeprecationInfo> getDeprecationInfo();


        interface AttributeDeprecationInfo {

            Optional<XPathAttributeMetadata> getReplacement();


            PmdVersion getSince();

        }

        interface PmdVersion {

        }
    }


}
