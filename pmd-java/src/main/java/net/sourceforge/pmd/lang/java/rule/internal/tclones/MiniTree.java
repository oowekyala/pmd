/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public final class MiniTree {

    private final MiniTree[] children;
    private final int mass;
    private final int hash;

    private MiniTree(MiniTree[] children, int mass, int hash) {
        this.children = children;
        this.mass = mass;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MiniTree miniTree = (MiniTree) o;
        return hash == miniTree.hash
            && mass == miniTree.mass; // i hope no chance of collision
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public int hash() {
        return hash;
    }

    /**
     * Size of the subtree, in number of nodes.
     */
    public int mass() {
        return mass;
    }

    public static final class MiniTreeBuilder {

        private static final int KIND_H = 1097; // some random prime number

        private final List<MiniTree> children;
        private int hash;
        private int mass;

        public MiniTreeBuilder() {
            mass = 1; // for the self node
            children = new ArrayList<>(2);
        }

        public void addChild(MiniTree mtree) {
            children.add(mtree);
            hash *= 31 * mtree.hash;
            mass += mtree.mass;
        }


        public MiniTreeBuilder hashAttr(String label, Object attribute) {
            return hashInt(label, Objects.hashCode(attribute));
        }

        public MiniTreeBuilder hashInt(String label, int attrHash) {
            this.hash += label.hashCode() * 17 * (attrHash + 1);
            return this;
        }

        public MiniTree buildAndReset() {
            MiniTree[] children = this.children.toArray(this.children.toArray(new MiniTree[0]));
            MiniTree result = new MiniTree(children, mass, hash);
            this.reset();
            return result;
        }

        private void reset() {
            hash = 0;
            mass = 1;
            children.clear();
        }

        public void hashKind(int productionID) {
            hash = KIND_H * (productionID + 1);
        }
    }
}
