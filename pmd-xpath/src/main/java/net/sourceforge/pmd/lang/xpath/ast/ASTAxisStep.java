/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.List;

// @formatter:off
/**
 * An axis step of a {@linkplain ASTPathExpr path expression}. One of the concrete types
 * of {@link StepExpr}.
 *
 * <p>The following shorthand notations are specified:
 * <ul>
 *     <li>"@" is a shorthand for the "attribute::" axis.
 *     <li>".." is a shorthand for the "parent::node()" step.
 *     <li>If the axis name is omitted from an axis step, the default axis is "child",
 *         with two exceptions: if the NodeTest in an axis step contains an AttributeTest
 *         or SchemaAttributeTest then the default axis is attribute. if the NodeTest in an
 *         axis step is a NamespaceNodeTest then a static error is raised. <a href="https://www.w3.org/TR/xpath-30/#doc-xpath30-AbbrevForwardStep">Source.</a>
 * </ul>
 * The subtree for these shorthand notations is exactly the same as that of the expanded forms.
 * This is implemented in {@link #jjtClose()}.
 *
 * <pre>
 *
 * (: Note that Axis is not a node and is not present in the children. :)
 * (: Note that NodeTest is an interface and the child is a concrete instance of an implementor. :)
 * AxisStep ::= ({@link Axis} "::")? {@link NodeTest} ({@linkplain ASTPredicate Predicate})*
 *
 * </pre>
 */
// @formatter:on
public final class ASTAxisStep extends AbstractXPathNode implements StepExpr {

    Axis axis; // parser only
    private boolean isAbbrevAttributeAxis;
    private boolean isAbbrevParentNodeTest;
    private boolean isAbbrevDescendantOrSelfStep;

    private boolean isAbbrevNoAxis;


    ASTAxisStep(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    /**
     * Returns true if this step didn't add an explicit axis,
     * in which case most of the time the child axis is implied.
     */
    public boolean isAbbrevNoAxis() {
        return isAbbrevNoAxis;
    }


    /**
     * Gets the axis of this step.
     */
    public Axis getAxis() {
        return axis;
    }


    /**
     * Returns true if this step uses the shorthand "..".
     */
    public boolean isAbbrevParentNodeTest() {
        return isAbbrevParentNodeTest;
    }


    @Override
    public void jjtClose() {
        super.jjtClose();
        if (isAbbrevParentNodeTest()) {
            insertSyntheticChild(SyntheticNodeFactory.synthesizeNodeTest("node()"), 0);
        } else if (isAbbrevNoAxis()) {
            // then the implied axis depends on the nodeTest

            // if it's an instance of AttributeTest we already handle that in the parser
            // and this method is not visited
            if (getNodeTest() instanceof ASTAttributeTest
                    || getNodeTest() instanceof ASTSchemaAttributeTest) {
                this.axis = Axis.ATTRIBUTE;
            } else if (getNodeTest() instanceof ASTNamespaceNodeTest) {
                parser.throwParseException("Namespace tests are illegal when not mentioning an axis.");
            }
        }
    }


    /**
     * Returns true if this step uses the shorthand "@".
     */
    public boolean isAbbrevAttributeAxis() {
        return isAbbrevAttributeAxis;
    }


    void setAbbrevNoAxis() {
        this.isAbbrevNoAxis = true;
        this.axis = Axis.CHILD;
    }


    void setAbbrevAttributeAxis() {
        this.axis = Axis.ATTRIBUTE;
        this.isAbbrevAttributeAxis = true;
    }


    /** Adds a synthetic nodetest to the children. */
    void setAbbrevParentNodeTest() {
        this.axis = Axis.PARENT;
        this.isAbbrevParentNodeTest = true;
    }


    /**
     * Returns the node test. Note that it is not a concrete node,
     * but an interface. The returned node is of a different concrete
     * type. Never null.
     */
    public NodeTest getNodeTest() {
        return (NodeTest) jjtGetChild(0);
    }


    /**
     * Gets the list of predicates of this step.
     */
    @Override
    public List<ASTPredicate> getPredicates() {
        return findChildrenOfType(ASTPredicate.class);
    }


    @Override
    public boolean isAbbrevDescendantOrSelf() {
        return isAbbrevDescendantOrSelfStep;
    }


    void setAbbrevDescendantOrSelf() {
        isAbbrevDescendantOrSelfStep = true;
        this.axis = Axis.DESCENDANT_OR_SELF;
        // This is not done in jjtClose because the parser closes the node before the call to this method
        // Besides, since AbbrevPathOperator has only one token and no children, this is safe
        NodeTest step = SyntheticNodeFactory.synthesizeNodeTest("node()");
        insertSyntheticChild(step, 0);
    }
}
/* JavaCC - OriginalChecksum=1b2f7cc49a50ed5ffaad09284531e531 (do not edit this line) */
