/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


/**
 * Step of a {@linkplain ASTPathExpr path expression}.
 * A step is either an {@linkplain ASTAxisStep axis step} or
 * a {@linkplain ASTPostfixExpr postfix expression}.
 *
 * <p>The syntax "//" to separate steps is actually shorthand for the
 * axis step "descendant-or-self::node()". The AST for this production
 * is exactly equivalent. See {@link #isAbbrevDescendantOrSelf()}.
 *
 * <pre>
 *
 * StepExpr ::= {@linkplain ASTPostfixExpr PostfixExpr} | {@linkplain ASTAxisStep AxisStep}
 *
 * (: Not a node. This production produces a StepExpr equivalent to "descendant-or-self::node()":)
 * AbbrevDescendantOrSelfStep ::= "//"
 *
 * </pre>
 */
public final class ASTStepExpr extends AbstractXPathNode {

    private boolean isAbbrevDescendantOrSelf;


    ASTStepExpr(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if this step expr is an abbreviated descendant or self step.
     * This is written "//" in the expression, but expands to "/descendant-or-self::node()/".
     * The structure of a "//" node is exactly identical to "/descendant-or-self::node()/".
     */
    public boolean isAbbrevDescendantOrSelf() {
        return isAbbrevDescendantOrSelf;
    }


    void setAbbrevDescendantOrSelf() {
        isAbbrevDescendantOrSelf = true;
        // This is not done in jjtClose because the parser closes the node before the call to this method
        // Besides, since AbbrevPathOperator has only one token and no children, this is safe
        ASTAxisStep step = SyntheticNodeFactory.synthesizeAxisStep("descendant-or-self::node()");
        insertChild(step, 0);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Returns true if this step is a postfix expression,
     * i.e. not an axis step.
     */
    public boolean isPostfixExpr() {
        return !isAxisStep();
    }


    /**
     * Returns true if this step is an axis step, i.e.
     * not a postfix expr.
     */
    public boolean isAxisStep() {
        return jjtGetChild(0) instanceof ASTAxisStep;
    }
}
/* JavaCC - OriginalChecksum=7ace41bad368a1c58ad4264392088d8d (do not edit this line) */
