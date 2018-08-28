/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Iterates over the children of a node that are of a specific type.
 *
 * @author Clément Fournier
 * @since 6.3.0
 */
class NodeChildrenIterator<T extends Node> implements Iterator<T> {

    private final Node parent;
    private final Class<T> targetChildType;
    private int i = 0;


    NodeChildrenIterator(Node parent, Class<T> targetChildType) {
        this.parent = parent;
        this.targetChildType = targetChildType;
    }


    private void moveToNext() {
        while (i < parent.jjtGetNumChildren() && !(targetChildType.isInstance(parent.jjtGetChild(i)))) {
            i++;
        }
    }


    @Override
    public boolean hasNext() {
        moveToNext();
        return i < parent.jjtGetNumChildren();
    }


    @Override
    public T next() {
        moveToNext();

        @SuppressWarnings("unchecked")
        T t = (T) parent.jjtGetChild(i++);
        return t;
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
