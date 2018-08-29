/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Iteration axis.
 *
 * <pre>
 *
 * (: Not a node :)
 * Axis ::= "self"
 *        | "child"
 *        | "attribute"
 *        | "descendant"
 *        | "descendant-or-self"
 *        | "ancestor"
 *        | "ancestor-or-self"
 *        | "following"
 *        | "following-sibling"
 *        | "namespace"
 *        | "parent"
 *        | "preceding"
 *        | "preceding-sibling"
 *
 * </pre>
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public enum Axis {
    SELF("self", true),
    CHILD("child", true),
    ATTRIBUTE("attribute", true) {
        @Override
        public XmdNodeKind getPrincipalNodeKind() {
            return XmdNodeKind.ATTRIBUTE;
        }
    },
    FOLLOWING("following", true),
    FOLLOWING_SIBLING("following-sibling", true),
    NAMESPACE("namespace", true) {
        @Override
        public XmdNodeKind getPrincipalNodeKind() {
            return XmdNodeKind.NAMESPACE;
        }
    },
    DESCENDANT("descendant", true),
    DESCENDANT_OR_SELF("descendant-or-self", true),

    // Reverse axes
    ANCESTOR("ancestor", false),
    ANCESTOR_OR_SELF("ancestor-or-self", false),
    PARENT("parent", false),
    PRECEDING("preceding", false),
    PRECEDING_SIBLING("preceding-sibling", false);


    private final String name;
    private final boolean isForward;


    Axis(String name, boolean isForward) {
        this.name = name;
        this.isForward = isForward;
    }


    /**
     * Returns whether this is a forward axis or not.
     * An axis that only ever contains the context node
     * or nodes that are after the context node in document
     * order is a forward axis.
     */
    public boolean isForward() {
        return isForward;
    }


    /**
     * Returns whether this is a reverse axis or not.
     * An axis that only ever contains
     * the context node or nodes that are before the context
     * node in document order is a reverse axis.
     */
    public boolean isReverse() {
        return !isForward();
    }


    /**
     * Returns the textual name of the axis.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the principal node kind of this axis.
     * Every axis has a principal node kind. If an
     * axis can contain elements, then the principal
     * node kind is element; otherwise, it is the kind
     * of nodes that the axis can contain.
     */
    public XmdNodeKind getPrincipalNodeKind() {
        return XmdNodeKind.ELEMENT;
    }


    /**
     * Returns the opposite axis.
     */
    public Axis opposite() {
        return null; // FIXME
    }

}
