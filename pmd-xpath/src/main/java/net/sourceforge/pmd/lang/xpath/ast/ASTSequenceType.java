/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Sequence type. Sequence types are used whenever it is necessary to
 * refer to a type in an XPath 3.0 expression. The term sequence type
 * suggests that this syntax is used to describe the type of an XPath
 * 3.0 value, which is always a sequence.
 *
 * <p>A special type "empty-sequence()" denotes the empty sequence.
 *
 * <p>With the exception of the special type empty-sequence(), a sequence
 * type consists of an item type that constrains the type of each item in
 * the sequence, and a cardinality that constrains the number of items in
 * the sequence.
 *
 * <pre>
 *
 * SequenceType ::= "empty-sequence" "(" ")"
 *                | {@link ItemType} ( "?" | "*" | "+" )?
 *
 * </pre>
 */
public final class ASTSequenceType extends AbstractXPathNode {

    /** Constructor for synthetic node. */
    public ASTSequenceType() {
        super(null, XPathParserTreeConstants.JJTSEQUENCETYPE);
    }

    // the setter is called on ?+*, so this is the default
    private Cardinality cardinality = Cardinality.EXACTLY_ONE;


    ASTSequenceType(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the cardinality of this type. If this type is the
     * empty sequence, then returns null.
     */
    public Cardinality getCardinality() {
        return cardinality;
    }


    /**
     * Returns true if this is the empty sequence.
     */
    public boolean isEmptySequence() {
        return cardinality == null;
    }


    /**
     * Returns the type of the items of this sequence.
     * If this type is the empty sequence, then returns null.
     * Check with {@link #isEmptySequence()}.
     */
    public ItemType getItemType() {
        return isEmptySequence() ? null : (ItemType) jjtGetChild(0);
    }


    void setCardinality(Cardinality card) {
        this.cardinality = card;
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=d53954607fe0124e2dded5a96f8b404b (do not edit this line) */
