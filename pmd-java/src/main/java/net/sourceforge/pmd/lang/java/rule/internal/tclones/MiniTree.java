/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sourceforge.pmd.util.document.FileLocation;
import net.sourceforge.pmd.util.document.Locator;
import net.sourceforge.pmd.util.document.TextRegion;

/**
 *
 */
public final class MiniTree {

    private final MiniTree[] children;
    private final int mass;
    private final int hash;
    private final int startOffset;
    private final int endOffset;
    private final Locator locator;

    private MiniTree(MiniTree[] children,
                     int mass,
                     int hash,
                     TextRegion region,
                     Locator locator) {
        this.children = children;
        this.mass = mass;
        this.hash = hash;
        this.startOffset = region.getStartOffset();
        this.endOffset = region.getEndOffset();
        this.locator = locator;
    }

    public FileLocation computeLocation() {
        return locator.toLocation(TextRegion.fromBothOffsets(startOffset, endOffset));
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

    /** Size of the subtree, in number of nodes. */
    public int mass() {
        return mass;
    }

    public static final class MiniTreeBuilder {

        private static final int KIND_H = 1097; // some random prime number

        private final List<MiniTree> children;
        private int hash;
        private int mass;

        private final Locator locator;

        public MiniTreeBuilder(Locator locator) {
            this.locator = locator;
            mass = 1; // for the self node
            hash = 1;
            children = new ArrayList<>(2);
        }

        public void addChild(MiniTree mtree) {
            children.add(mtree);
            hash *= 7 + 31 * mtree.hash;
            mass += mtree.mass;
        }


        public MiniTreeBuilder hashAttr(String label, Object attribute) {
            return hashInt(label, Objects.hashCode(attribute));
        }

        public MiniTreeBuilder hashInt(String label, int attrHash) {
            int h = this.hash;
            h *= 7;
            h += 13 * (3 + label.hashCode()) * 17 * (attrHash + 1);
            this.hash = h;
            return this;
        }

        public MiniTree buildAndReset(TextRegion region) {
            MiniTree[] children = this.children.toArray(this.children.toArray(new MiniTree[0]));
            MiniTree result = new MiniTree(children, mass, hash, region, locator);
            this.reset();
            return result;
        }

        private void reset() {
            hash = 1;
            mass = 1;
            children.clear();
        }

        public void hashKind(int productionID) {
            hash = KIND_H * (productionID + 1);
        }

        public MiniTreeBuilder childrenBuilder() {
            return new MiniTreeBuilder(this.locator);
        }
    }
}
