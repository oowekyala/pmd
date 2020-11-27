/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.util.document.FileLocation;
import net.sourceforge.pmd.util.document.Locator;
import net.sourceforge.pmd.util.document.TextRegion;

/**
 *
 */
public final class MiniTree {

    private final MiniTree[] children;
    private final int mass;
    private final int deepHash;
    private final int kind;
    private final Map<String, Object> attributes;
    private final int startOffset;
    private final int endOffset;
    private final Locator locator;

    private MiniTree(MiniTree[] children,
                     int mass,
                     int hash,
                     int kind,
                     Map<String, Object> attributes,
                     TextRegion region,
                     Locator locator) {
        this.attributes = attributes;
        assert kind != -1 : "Kind was not set";
        this.children = children;
        this.mass = mass;
        this.deepHash = hash;
        this.kind = kind;
        this.startOffset = region.getStartOffset();
        this.endOffset = region.getEndOffset();
        this.locator = locator;
    }

    public FileLocation computeLocation() {
        return locator.toLocation(getRegion());
    }

    public void foreachDescendantAboveMass(int minMass, Consumer<MiniTree> action) {
        for (MiniTree child : children) {
            if (child.mass >= minMass) {
                action.accept(this);
                child.foreachDescendantAboveMass(minMass, action);
            }
        }
    }

    /**
     * Similarity is the proportion of nodes both trees have in common.
     */
    public double similarity(MiniTree other) {
        return 2 * numCommonNodes(this, other)
            / (double) (this.mass + other.mass);
    }

    private boolean shallowEquals(MiniTree other) {
        return this.kind == other.kind && other.attributes.equals(this.attributes);
    }

    public TextRegion getRegion() {
        return TextRegion.fromBothOffsets(startOffset, endOffset);
    }

    private static int numCommonNodes(MiniTree t1, MiniTree t2) {
        int result = 0;
        if (t1.shallowEquals(t2)) {
            result++;
        }

        if (t1.children.length > t2.children.length) {
            MiniTree tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        int t1Len = t1.children.length;
        // t1 is now the node with the fewest children

        for (int i = 0; i < t1Len; i++) {
            result += numCommonNodes(t1.children[i], t2.children[i]);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return deepHash; //this is relevant to put it in the buckets of the hashmap
    }

    /** The hash includes the whole subtree. */
    public int deepHash() {
        return deepHash;
    }

    /** Size of the subtree, in number of nodes. */
    public int mass() {
        return mass;
    }

    public static final class MiniTreeBuilder {

        private final List<MiniTree> children;
        private int hash;
        private int mass;
        private int kind;
        private Map<String, Object> attributes;

        private final Locator locator;

        public MiniTreeBuilder(Locator locator) {
            this.locator = locator;
            children = new ArrayList<>(2);
            reset();
        }

        public void addChild(MiniTree mtree) {
            children.add(mtree);
            hash *= 7 + 31 * mtree.deepHash;
            mass += mtree.mass;
        }


        public MiniTreeBuilder hashAttr(String label, @Nullable Object value) {
            recordAttr(label, value);
            return hashInt(label, Objects.hashCode(value));
        }

        /**
         * Add an attribute that will NOT be hashed but will count
         * towards similarity.
         */
        public void addAttrWithoutHash(String label, @Nullable Object value) {
            recordAttr(label, value);
        }

        private void recordAttr(String label, @Nullable Object value) {
            if (attributes == null) {
                attributes = new HashMap<>(1);
            }
            attributes.put(label, value);
        }


        public void hashKind(int productionID) {
            this.kind = productionID;
            hashInt("", productionID);
        }

        private MiniTreeBuilder hashInt(String label, int attrHash) {
            int h = this.hash;
            h *= 7;
            h += 13 * (3 + label.hashCode()) * 17 * (attrHash + 1);
            this.hash = h;
            return this;
        }

        public MiniTree buildAndReset(TextRegion region) {
            MiniTree[] children = this.children.toArray(this.children.toArray(new MiniTree[0]));
            Map<String, Object> attributes = this.attributes;
            if (attributes == null) {
                attributes = Collections.emptyMap();
            }
            MiniTree result = new MiniTree(children, mass, hash, kind, attributes, region, locator);
            this.reset();
            return result;
        }

        private void reset() {
            hash = 1;
            mass = 1;
            children.clear();
            kind = -1;
            attributes = null;
        }

    }
}
