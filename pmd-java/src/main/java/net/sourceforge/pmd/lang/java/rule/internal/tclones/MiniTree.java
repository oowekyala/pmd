/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.document.Locator;
import net.sourceforge.pmd.lang.document.TextRegion;

/**
 * A light-weight mirror of an AST, that can be kept in memory when we're
 * looking for clones.
 */
public final class MiniTree {

    private static final MiniTree[] EMPTY = new MiniTree[0];

    // total size of this subtree
    private final int mass;

    // state used only in the initial partitioning phase
    private final int deepHash;

    // state used only in the similarity comparison phase
    private final int kind;
    private final List<Object> attributes;
    private final MiniTree[] children;

    // state used to locate the node, only used if it is identified as a clone
    private final int startOffset;
    private final int endOffset;
    private final Locator locator;

    private MiniTree(MiniTree[] children,
                     int mass,
                     int hash,
                     int kind,
                     List<Object> attributes,
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

    public FileLocation fetchLocation() {
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
        private List<Object> attributes;

        private final Locator locator;

        public MiniTreeBuilder(Locator locator) {
            this.locator = locator;
            children = new ArrayList<>(2);
            reset();
        }

        void addChild(MiniTree mtree) {
            children.add(mtree);
            hash *= 7 + 31 * mtree.deepHash;
            mass += mtree.mass;
        }

        /**
         * Record an attribute that will count during both the structural
         * pruning (hash phase) and the similarity phase.
         */
        public MiniTreeBuilder hashAttr(String label, @Nullable Object value) {
            recordAttr(label, value);
            return hashInt(label, Objects.hashCode(value));
        }

        // values with perfect hashes do not need to be recorded for later comparison

        /** Enums have perfect hashes. */
        public MiniTreeBuilder perfectHashAttr(String label, @NonNull Enum<?> value) {
            return hashInt(label, Objects.hashCode(value));
        }

        /** Integers have perfect hashes. */
        public MiniTreeBuilder perfectHashAttr(String label, int value) {
            return hashInt(label, value);
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
                attributes = new ArrayList<>(1);
            }

            attributes.add(value);
        }


        void hashKind(int productionID) {
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
            MiniTree[] children = this.children.toArray(EMPTY);
            List<Object> attributes = this.attributes;
            if (attributes == null) {
                attributes = Collections.emptyList();
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
