package net.sourceforge.pmd.lang.rule;

import net.sourceforge.pmd.reporting.RuleViolation;

public interface GlobalAnalysisRule extends Rule {

    void endAnalysis(GlobalReportingContext ctx);

    interface GlobalReportingContext {
        void reportViolation(RuleViolation violation);
    }
}
