/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.tclones;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class TreeCloneLogger {

    static final boolean RECORD = true;
    private final IntSummaryStatistics hitStats = new IntSummaryStatistics();
    private final IntSummaryStatistics missStats = new IntSummaryStatistics();

    private final int printPeriod;
    private final AtomicInteger fileCount = new AtomicInteger();


    TreeCloneLogger(int printPeriod) {
        this.printPeriod = printPeriod;
    }

    void endFile(CloneDetectorGlobals globals) {
        if (RECORD) {
            int curFile = fileCount.incrementAndGet();
            if (curFile % printPeriod == 0) {
                line("File %d", curFile);
                print(globals);
            }
        }
    }

    void endAll(CloneDetectorGlobals globals) {
        if (RECORD) {
            line("End (%d)", fileCount.get());
            print(globals);
        }
    }

    private void print(CloneDetectorGlobals globals) {
        line("Miss    |  %s", missStats);
        line("Hit     |  %s", hitStats);
        line("Total   |  %s", makeTotal());
        IntSummaryStatistics bucketStats = getBucketStats(globals);
        line("Buckets | %s", bucketStats);
        line("");
    }


    void record(int mass, boolean considered) {
        if (RECORD) { // otherwise noop, constant folded by jit
            if (considered) {
                hitStats.accept(mass);
            } else {
                missStats.accept(mass);
            }
        }
    }

    private IntSummaryStatistics makeTotal() {
        IntSummaryStatistics total = new IntSummaryStatistics();
        total.combine(missStats);
        total.combine(hitStats);
        return total;
    }

    private IntSummaryStatistics getBucketStats(CloneDetectorGlobals globals) {
        return globals.buckets.values()
                              .stream().mapToInt(List::size)
                              .summaryStatistics();
    }

    private void line(String pattern, Object... args) {
        System.err.printf(pattern + "%n", args);
    }

}
