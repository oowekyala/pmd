/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 */
final class CloneDetectorGlobals {

    final ConcurrentHashMap<Integer, List<MiniTree>> buckets = new ConcurrentHashMap<>();
    final int minMass;
    final double simThreshold;
    final TreeCloneLogger logger = new TreeCloneLogger(400);

    CloneDetectorGlobals(int minMass, double simThreshold) {
        this.minMass = minMass;
        this.simThreshold = simThreshold;
    }

    private static int bucketH(int treeH) {
        return treeH;
    }

    public void acceptTree(MiniTree tree) {

        boolean considered = isBigEnough(tree);

        logger.record(tree.mass(), considered);

        if (considered) {
            // `compute` guarantees atomic access to the list so it doesn't need synchronization
            buckets.compute(bucketH(tree.deepHash()),
                            (key, list) -> appendList(tree, list));
        }
    }

    private boolean isBigEnough(MiniTree tree) {
        return tree.mass() >= minMass;
    }

    private static <T> List<T> appendList(T tree, List<T> list) {
        if (list == null) {
            list = new ArrayList<>(1);
        }
        list.add(tree);
        return list;
    }


    void endFile() {
        logger.endFile(this);
    }

    static final class CloneSet {

        final Map<MiniTree, List<MiniTree>> clones = new HashMap<>();

        void removeClonesOf(MiniTree t) {

        }

        void addClonePair(MiniTree t1, MiniTree t2) {
            if (System.identityHashCode(t2) < System.identityHashCode(t1)) {
                // canonicalize order, to speedup containment test
                // todo this should be repeatable across runs
                addClonePair(t2, t1);
                return;
            }

            clones.compute(t1, (k, list) -> appendList(t2, list));
        }
    }

    void computeDuplicates() {
        // remove trees that are not duplicated
        buckets.values().removeIf(l -> l.size() < 2);

        CloneSet cloneSet = initialCloneSet();
        logger.endAll(this);

        cloneSet.clones.forEach((key, others) -> {
            System.out.println(key.computeLocation());
            others.forEach(it -> System.out.println("    " + it.computeLocation()));
        });
    }

    private @NonNull CloneSet initialCloneSet() {
        CloneSet cloneSet = new CloneSet();
        buckets.values().forEach(bucket -> {
            for (int i = 0; i < bucket.size(); i++) {
                MiniTree ti = bucket.get(i);
                // j = i+1 (triangular iteration)
                for (int j = i + 1; j < bucket.size(); j++) {
                    MiniTree tj = bucket.get(j);
                    double similarity = ti.similarity(tj);
                    if (similarity >= simThreshold) {
                        // remove strictly smaller clones
                        ti.foreachDescendantAboveMass(minMass, cloneSet::removeClonesOf);
                        tj.foreachDescendantAboveMass(minMass, cloneSet::removeClonesOf);
                        // finally add the clone pair
                        cloneSet.addClonePair(ti, tj);
                    }
                }
            }
        });
        return cloneSet;
    }
}
