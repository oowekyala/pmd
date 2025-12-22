/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

import java.util.HashMap;
import java.util.Map;

/**
 * An object to run value analyses on code. Analysis can depend on each
 * other and call each other. This class stores all the currently running
 * analyses.
 */
public class AnalysisEngine {

    private final ReachingDefinitionsAnalysis.DataflowResult dataflow;
    private final Map<Class<?>, ValueAnalysis<?>> allAnalyses = new HashMap<>();

    AnalysisEngine(ReachingDefinitionsAnalysis.DataflowResult dataflow) {
        this.dataflow = dataflow;
    }

    public <A extends ValueAnalysis<?>> void register(A analysis) {
        allAnalyses.put(analysis.getClass(), analysis);
        analysis.setEngine(this);
    }

    <A extends ValueAnalysis<?>> A getAnalysis(Class<A> analysisClass) {
        @SuppressWarnings("unchecked")
        A dfAnalysis = (A) allAnalyses.get(analysisClass);
        if (dfAnalysis == null) {
            throw new IllegalArgumentException("analysis not registered: " + analysisClass.getName());
        }
        return dfAnalysis;
    }

    ReachingDefinitionsAnalysis.DataflowResult getDataflow() {
        return dataflow;
    }
}
