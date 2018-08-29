/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

// @formatter:off
/**
 * An axis step of a {@linkplain ASTPathExpr path expression}.
 * One of the two possible children types for {@link ASTStepExpr}.
 *
 * <p>The following shorthand notations are specified:
 * <ul>
 *     <li>"@" is a shorthand for the "attribute::" axis.
 *     <li>".." is a shorthand for the "parent::node()" step.
 *     <li>If the axis name is omitted from an axis step, the default axis is "child",
 *         with two exceptions: if the NodeTest in an axis step contains an AttributeTest
 *         or SchemaAttributeTest then the default axis is attribute.
 * </ul>
 * The subtree for these shorthand notations is exactly the same as that of the expanded forms.
 *
 * <pre>
 *
 * AxisStep ::= (Axis "::")? {@linkplain ASTNodeTest NodeTest} {@linkplain ASTPredicateList PredicateList}
 *
 * (: Not a node :)
 * Axis ::= {@linkplain Axis Axis}
 *
 * </pre>
 */
// @formatter:on
public final class ASTAxisStep extends AbstractXPathNode {

    Axis axis; // parser only
    private boolean isAbbrevAttributeAxis;
    private boolean isAbbrevParentNodeTest;

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
            insertChild(SyntheticNodeFactory.synthesizeNodeTest("node()"), 0);
        }
    }


    /**
     * Returns true if this step uses the shorthand "@".
     */
    public boolean isAbbrevAttributeAxis() {
        return isAbbrevAttributeAxis;
    }


    void setAbbrevNoAxis() {
        this.axis = Axis.CHILD; // FIXME
        this.isAbbrevNoAxis = true;
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
     * Returns the node test.
     */
    public ASTNodeTest getNodeTest() {
        return (ASTNodeTest) jjtGetChild(0);
    }


    /**
     * Gets the list of predicates of this step.
     */
    public ASTPredicateList getPredicates() {
        return (ASTPredicateList) jjtGetChild(1);
    }

}
/* JavaCC - OriginalChecksum=1b2f7cc49a50ed5ffaad09284531e531 (do not edit this line) */
