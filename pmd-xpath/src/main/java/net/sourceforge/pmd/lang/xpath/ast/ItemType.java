/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.KindTest;


/**
 * Type of an item of a {@linkplain ASTSequenceType SequenceType}.
 * Apart from the item type {@linkplain ASTAnyItemType item()},
 * which permits any kind of item, item types divide into {@linkplain KindTest node types}
 * (such as {@linkplain ASTElementTest element()}), {@linkplain ASTAtomicOrUnionType generalized atomic types}
 * (such as {@code xs:integer}) and {@linkplain FunctionTest function types} (such as {@code function() as item()*}).
 *
 *
 * <pre>
 *
 * ItemType ::= {@link KindTest KindTest}
 *            | {@linkplain ASTAnyItemType AnyItemType}
 *            | {@link FunctionTest}
 *            | {@linkplain ASTAtomicOrUnionType AtomicOrUnionType}
 *            | {@linkplain ASTParenthesizedItemType ParenthesizedItemType}
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface ItemType extends XPathNode {


    /**
     * Function type.
     *
     * <pre>
     *
     * FunctionTest ::= {@linkplain ASTAnyFunctionTest AnyFunctionTest}
     *                | {@linkplain ASTTypedFunctionTest TypedFunctionTest}
     *
     * </pre>
     */
    interface FunctionTest extends ItemType {

    }

}
