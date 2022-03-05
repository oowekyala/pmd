/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.SuppressedViolation;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.ViolationSuppressor;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.JavaRuleViolation;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import net.sourceforge.pmd.lang.rule.impl.DefaultRuleViolationFactory;

public final class JavaRuleViolationFactory extends DefaultRuleViolationFactory {

    public static final RuleViolationFactory INSTANCE = new JavaRuleViolationFactory();
    private static final ViolationSuppressor JAVA_ANNOT_SUPPRESSOR = new ViolationSuppressor() {
        @Override
        public String getId() {
            return "@SuppressWarnings";
        }

        @Override
        public Report.SuppressedViolation suppressOrNull(RuleViolation rv, @NonNull Node node) {
            if (AnnotationSuppressionUtil.contextSuppresses(node, rv.getRule())) {
                return new SuppressedViolation(rv, this, null);
            }
            return null;
        }
    };

    private JavaRuleViolationFactory() {
        // singleton
    }

    @Override
    protected List<ViolationSuppressor> getSuppressors() {
        return Collections.singletonList(JAVA_ANNOT_SUPPRESSOR);
    }

    @Override
    public RuleViolation createViolation(Rule rule, @NonNull Node node, FileLocation location, @NonNull String formattedMessage) {
        if (node instanceof JavaNode) {
            return new JavaRuleViolation(rule, (JavaNode) node, location, formattedMessage);
        }
        return super.createViolation(rule, node, location, formattedMessage);
    }

}
