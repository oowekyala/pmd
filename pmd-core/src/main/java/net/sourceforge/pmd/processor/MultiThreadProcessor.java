/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.processor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.reporting.GlobalAnalysisListener;


/**
 * @author Romain Pelisse &lt;belaran@gmail.com&gt;
 */
final class MultiThreadProcessor extends AbstractPMDProcessor {

    private final ExecutorService executor;


    MultiThreadProcessor(final PMDConfiguration configuration, RuleSets ruleSets) {
        super(configuration, ruleSets);

        executor = Executors.newFixedThreadPool(configuration.getThreads(), new PmdThreadFactory());
    }


    @Override
    @SuppressWarnings("PMD.CloseResource") // closed by the PMDRunnable
    public void processFiles(List<TextFile> files, GlobalAnalysisListener listener) {
        // The thread-local is not static, but analysis-global
        // This means we don't have to reset it manually, every analysis is isolated.
        // The initial value makes a copy of the rulesets
        final ThreadLocal<RuleSets> ruleSetCopy = ThreadLocal.withInitial(() -> new RuleSets(this.ruleSets));

        for (final TextFile dataSource : files) {
            executor.submit(new PmdRunnable(dataSource, listener, configuration) {
                @Override
                protected RuleSets getRulesets() {
                    return ruleSetCopy.get();
                }
            });
        }
    }

    @Override
    public void close() {
        try {
            executor.shutdown();
            while (!executor.awaitTermination(10, TimeUnit.HOURS)) {
                // still waiting
                Thread.yield();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }
        super.close();
    }
}
