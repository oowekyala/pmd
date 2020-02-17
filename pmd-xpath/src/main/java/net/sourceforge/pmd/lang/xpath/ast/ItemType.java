/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Type of an item of a {@link ASTSequenceType SequenceType}.
 * Apart from the item type {@link ASTAnyItemType item()},
 * which permits any kind of item, item types divide into {@link KindTest node types}
 * (such as {@link ASTElementTest element()}), {@link ASTAtomicOrUnionType generalized atomic types}
 * (such as {@code xs:integer}) and {@link FunctionTest function types} (such as {@code function() as item()*}).
 *
 *
 * <pre>
 *
 * ItemType ::= {@link KindTest KindTest}
 *            | {@link ASTAnyItemType AnyItemType}
 *            | {@link FunctionTest}
 *            | {@link ASTAtomicOrUnionType AtomicOrUnionType}
 *            | {@link ASTParenthesizedItemType ParenthesizedItemType}
 *
 * </pre>
 */
public interface ItemType extends XPathNode {


    /**
     * Function type.
     *
     * <pre>
     *
     * FunctionTest ::= {@link ASTAnyFunctionTest AnyFunctionTest}
     *                | {@link ASTTypedFunctionTest TypedFunctionTest}
     *
     * </pre>
     */
    interface FunctionTest extends ItemType {

    }

}
