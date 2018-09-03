/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Collections;
import java.util.List;


/**
 * Step of a {@linkplain ASTPathExpr path expression}.
 *
 * <p>The syntax "//" to separate steps is actually shorthand for the
 * axis step "descendant-or-self::node()". The AST for this production
 * is exactly equivalent. See {@link #isAbbrevDescendantOrSelf()}.
 *
 * <pre>
 *
 * StepExpr ::= {@linkplain ASTPostfixExpr PostfixExpr}
 *            | {@linkplain ASTAxisStep AxisStep}
 *            | {@linkplain PrimaryExpr}
 *
 * (: This production produces an AxisStep equivalent to "descendant-or-self::node()" :)
 * AbbrevDescendantOrSelfStep ::= "//"
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public interface StepExpr extends XPathNode {

    /**
     * Returns true if this step expr is an abbreviated descendant or self step.
     * This is written "//" in the expression, but expands to "/descendant-or-self::node()/".
     * The structure of the subtree is exactly identical to the expanded form.
     */
    default boolean isAbbrevDescendantOrSelf() {
        return false;
    }


    /**
     * Gets the predicates applying to this step.
     */
    default List<ASTPredicate> getPredicates() {
        return Collections.emptyList();
    }


    default boolean isAxisStep() {
        return this instanceof ASTAxisStep;
    }


    default boolean isPostfixExpr() {
        return this instanceof ASTPostfixExpr;
    }


    default boolean isPrimaryExpr() {
        return this instanceof PrimaryExpr;
    }

}
