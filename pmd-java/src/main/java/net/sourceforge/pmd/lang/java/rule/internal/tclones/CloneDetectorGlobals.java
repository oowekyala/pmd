/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
final class CloneDetectorGlobals {

    final ConcurrentHashMap<Integer, List<MiniTree>> buckets = new ConcurrentHashMap<>();
    final int minConsideredSize;
    final TreeCloneLogger logger = new TreeCloneLogger(400);

    CloneDetectorGlobals(int minConsideredSize) {
        this.minConsideredSize = minConsideredSize;
    }

    private static int bucketH(int treeH) {
        return treeH;
    }

    public void acceptTree(MiniTree tree) {
        int mass = tree.mass();

        boolean considered = mass >= minConsideredSize;

        logger.record(mass, considered);

        if (considered) {
            // `compute` guarantees atomic access to the list so it doesn't need synchronization
            buckets.compute(bucketH(tree.hash()),
                            (key, list) -> appendList(tree, list));
        }
    }

    private static List<MiniTree> appendList(MiniTree tree, List<MiniTree> list) {
        if (list == null) {
            list = new ArrayList<>(1);
        }
        list.add(tree);
        return list;
    }


    void endFile() {
        logger.endFile(this);
    }


    void computeDuplicates() {
        // remove trees that are not duplicated
        buckets.values().removeIf(l -> l.size() < 2);

        logger.endAll(this);
    }
}
