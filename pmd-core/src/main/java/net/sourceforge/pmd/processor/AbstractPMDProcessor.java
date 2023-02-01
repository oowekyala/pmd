/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.processor;

import static net.sourceforge.pmd.util.CollectionUtil.listOf;

import java.util.List;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.GlobalAnalysisListener;

/**
 * This is internal API!
 *
 * @author Romain Pelisse &lt;belaran@gmail.com&gt;
 */
@InternalApi
public abstract class AbstractPMDProcessor implements AutoCloseable {

    protected final PMDConfiguration configuration;
    protected final RuleSets ruleSets;


    AbstractPMDProcessor(PMDConfiguration configuration, RuleSets ruleSets) {
        this.configuration = configuration;
        this.ruleSets = ruleSets;
    }

    /**
     * Analyse all files. Each text file is closed.
     */
    public abstract void processFiles(List<TextFile> files, GlobalAnalysisListener listener);


    /**
     * Joins tasks and await completion of the analysis. After this, all
     * {@link TextFile}s must have been closed.
     */
    @Override
    public void close() {
        this.ruleSets.getAllRules().forEach(r -> {
            RuleContext rctx = RuleContext.create(FileAnalysisListener.noop(), r);
            r.endAnalysis(rctx);
        });
    }

    /**
     * Returns a new file processor. The strategy used for threading is
     * determined by {@link PMDConfiguration#getThreads()}.
     */
    public static AbstractPMDProcessor newFileProcessor(final PMDConfiguration configuration, RuleSets ruleSets) {
        return configuration.getThreads() > 1 ? new MultiThreadProcessor(configuration, ruleSets)
                                              : new MonoThreadProcessor(configuration, ruleSets);
    }

    /**
     * This is provided as convenience for tests. The listener is not closed.
     * It executes the rulesets on this thread, without copying the rulesets.
     */
    @InternalApi
    public static void runSingleFile(List<RuleSet> ruleSets, TextFile file, GlobalAnalysisListener listener, PMDConfiguration configuration) {
        RuleSets rsets = new RuleSets(ruleSets);
        new MonoThreadProcessor(configuration, rsets).processFiles(listOf(file), listener);
    }
}
