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

    private final DataflowPass.DataflowResult dataflow;
    private final Map<Class<?>, DfAnalysis<?>> allAnalyses = new HashMap<>();

    AnalysisEngine(DataflowPass.DataflowResult dataflow) {
        this.dataflow = dataflow;
    }

    public <A extends DfAnalysis<?>> void register(A analysis) {
        allAnalyses.put(analysis.getClass(), analysis);
        analysis.setEngine(this);
    }

    <A extends DfAnalysis<?>> A getAnalysis(Class<A> analysisClass) {
        @SuppressWarnings("unchecked")
        A dfAnalysis = (A) allAnalyses.get(analysisClass);
        if (dfAnalysis == null) {
            throw new IllegalArgumentException("analysis not registered: " + analysisClass.getName());
        }
        return dfAnalysis;
    }

    DataflowPass.DataflowResult getDataflow() {
        return dataflow;
    }
}
