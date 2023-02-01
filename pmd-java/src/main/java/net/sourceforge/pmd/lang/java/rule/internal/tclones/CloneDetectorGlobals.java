/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.util.StringUtil;

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
                            (key, list) -> append(list, tree));
        }
    }

    private boolean isBigEnough(MiniTree tree) {
        return tree.mass() >= minMass;
    }

    private static <T> List<T> append(@Nullable List<T> list, T tree) {
        if (list == null) {
            list = new ArrayList<>(1);
        }
        list.add(tree);
        return list;
    }


    void endFile() {
        logger.endFile(this);
    }

    static final class CloneSpec {

        final double similarity;
        final MiniTree tree;

        CloneSpec(double similarity, MiniTree tree) {
            this.similarity = similarity;
            this.tree = tree;
        }

        public double similarity() {
            return similarity;
        }

        public FileLocation fetchLocation() {
            return tree.fetchLocation();
        }
    }

    static final class CloneSet {

        private static final int GC_GEN_SIZE = 300;
        final Map<MiniTree, List<CloneSpec>> clones = new HashMap<>();
        final Set<MiniTree> prunedTrees = new HashSet<>();

        void removeClonesOf(MiniTree t) {
            boolean ok = clones.remove(t) != null;

            // we need to remove it also in the values list
            // we only do that periodically, which is a big performance improvement,
            // both in space (the lambdas need to be garbage collected) and time
            if (!ok && prunedTrees.add(t) && prunedTrees.size() > GC_GEN_SIZE) {
                for (List<CloneSpec> cloneSpecs : clones.values()) {
                    cloneSpecs.removeIf(spec -> prunedTrees.contains(spec.tree));
                }
                prunedTrees.clear();
            }
        }

        void addClonePair(MiniTree t1, MiniTree t2, double similarity) {
            if (System.identityHashCode(t2) < System.identityHashCode(t1)) {
                // canonicalize order, to speedup containment test
                // todo this should be repeatable across runs
                addClonePair(t2, t1, similarity);
                return;
            }

            clones.compute(t1, (k, list) -> {
                if (list == null) {
                    list = new ArrayList<>(1);
                }
                list.add(new CloneSpec(similarity, t2));
                return list;
            });
        }

        int totalSize() {
            return clones.values().stream().mapToInt(it -> it.size() + 1).sum();
        }
    }

    void computeDuplicates() {
        // remove trees that are not duplicated
        buckets.values().removeIf(l -> l.size() < 2);

        CloneSet cloneSet = initialCloneSet();
        logger.endAll(this);

        cloneSet.clones.forEach((key, others) -> {
            others.sort(Comparator.comparingDouble(CloneSpec::similarity).reversed());

            System.out.println(key.fetchLocation());

            others.forEach(clone -> {
                String simPercent = StringUtil.percentageString(clone.similarity(), 2);
                System.out.println("    " + simPercent + " @ " + clone.fetchLocation());
            });
        });

        System.out.println(cloneSet.totalSize() + " clones in " + cloneSet.clones.size() + " buckets");
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
                        // remove strictly smaller clones because they're less interesting
                        ti.foreachDescendantAboveMass(minMass, cloneSet::removeClonesOf);
                        tj.foreachDescendantAboveMass(minMass, cloneSet::removeClonesOf);
                        // finally add the clone pair
                        cloneSet.addClonePair(ti, tj, similarity);
                    }
                }
            }
        });
        return cloneSet;
    }
}
