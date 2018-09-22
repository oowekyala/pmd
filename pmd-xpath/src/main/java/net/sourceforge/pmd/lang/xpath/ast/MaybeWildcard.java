/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Represents either a wildcard or a node.
 *
 * @param <T> Type of node to wrap
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public final class MaybeWildcard<T extends XPathNode> {


    private static final MaybeWildcard<?> WILDCARD = new MaybeWildcard<>(null);

    private final T value;


    MaybeWildcard(T value) {
        this.value = value;
    }


    /**
     * Returns true if this represents a wildcard.
     */
    public boolean isWildcard() {
        return value == null;
    }


    /**
     * Returns true if the node is there.
     */
    public boolean isPresent() {
        return !isWildcard();
    }


    /**
     * Returns the node. Returns null if this is a wildcard.
     */
    public T getValue() {
        return value;
    }


    /**
     * Returns a wildcard instance.
     */
    public static <U extends XPathNode> MaybeWildcard<U> wildcard() {
        @SuppressWarnings("unchecked")
        MaybeWildcard<U> w = (MaybeWildcard<U>) WILDCARD;
        return w;
    }

}
