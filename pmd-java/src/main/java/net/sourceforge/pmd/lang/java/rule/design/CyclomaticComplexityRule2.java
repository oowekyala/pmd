/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.design;

import static net.sourceforge.pmd.properties.constraints.NumericConstraints.positive;
import static net.sourceforge.pmd.util.CollectionUtil.listOf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.rule.RuleBehavior;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaVisitorBase;
import net.sourceforge.pmd.lang.java.ast.internal.PrettyPrintingUtil;
import net.sourceforge.pmd.lang.java.metrics.api.JavaClassMetricKey;
import net.sourceforge.pmd.lang.java.metrics.api.JavaOperationMetricKey;
import net.sourceforge.pmd.lang.java.metrics.internal.CycloMetric;
import net.sourceforge.pmd.lang.java.metrics.internal.CycloMetric.CycloOption;
import net.sourceforge.pmd.lang.metrics.MetricOptions;
import net.sourceforge.pmd.lang.metrics.MetricsUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;


/**
 * Cyclomatic complexity rule using metrics.
 *
 * @author Clément Fournier, based on work by Alan Hohn and Donald A. Leckie
 * @version 6.0.0
 * @see CycloMetric
 */
public class CyclomaticComplexityRule2 implements RuleBehavior {

    private static final PropertyDescriptor<Integer> CLASS_LEVEL_DESCRIPTOR
        = PropertyFactory.intProperty("classReportLevel")
                         .desc("Total class complexity reporting threshold")
                         .require(positive()).defaultValue(80).build();

    private static final PropertyDescriptor<Integer> METHOD_LEVEL_DESCRIPTOR
        = PropertyFactory.intProperty("methodReportLevel")
                         .desc("Cyclomatic complexity reporting threshold")
                         .require(positive()).defaultValue(10).build();

    private static final Map<String, CycloOption> OPTION_MAP;


    static {
        OPTION_MAP = new HashMap<>();
        OPTION_MAP.put(CycloOption.IGNORE_BOOLEAN_PATHS.valueName(), CycloOption.IGNORE_BOOLEAN_PATHS);
        OPTION_MAP.put(CycloOption.CONSIDER_ASSERT.valueName(), CycloOption.CONSIDER_ASSERT);
    }


    private static final PropertyDescriptor<List<CycloOption>> CYCLO_OPTIONS_DESCRIPTOR
        = PropertyFactory.enumListProperty("cycloOptions", OPTION_MAP)
                         .desc("Choose options for the computation of Cyclo")
                         .emptyDefaultValue()
                         .build();

    @Override
    public List<? extends PropertyDescriptor<?>> declaredProperties() {
        return listOf(CLASS_LEVEL_DESCRIPTOR, METHOD_LEVEL_DESCRIPTOR, CYCLO_OPTIONS_DESCRIPTOR);
    }

    @Override
    public RuleAnalyser initialize(PropertySource properties, Language language, RuleInitializationWarner warner) {
        int methodReportLevel = properties.getProperty(METHOD_LEVEL_DESCRIPTOR);
        int classReportLevel = properties.getProperty(CLASS_LEVEL_DESCRIPTOR);
        MetricOptions cycloOptions = MetricOptions.ofOptions(properties.getProperty(CYCLO_OPTIONS_DESCRIPTOR));

        return new VisitorAnalyser(new MyVisitor(cycloOptions, classReportLevel, methodReportLevel));
    }

    private static class MyVisitor extends JavaVisitorBase<RuleContext, Void> {

        private final MetricOptions cycloOptions;
        private final int classReportLevel;
        private final int methodReportLevel;

        public MyVisitor(MetricOptions cycloOptions, int classReportLevel, int methodReportLevel) {
            this.cycloOptions = cycloOptions;
            this.classReportLevel = classReportLevel;
            this.methodReportLevel = methodReportLevel;
        }

        @Override
        public Void visitTypeDecl(ASTAnyTypeDeclaration node, RuleContext ctx) {

            visitChildren(node, ctx);

            if (JavaClassMetricKey.WMC.supports(node)) {
                int classWmc = (int) MetricsUtil.computeMetric(JavaClassMetricKey.WMC, node, cycloOptions);

                if (classWmc >= classReportLevel) {
                    int classHighest = (int) MetricsUtil.computeStatistics(JavaOperationMetricKey.CYCLO, node.getOperations(), cycloOptions).getMax();

                    String[] messageParams = {PrettyPrintingUtil.kindName(node),
                                              node.getSimpleName(),
                                              " total",
                                              classWmc + " (highest " + classHighest + ")",};

                    ctx.addViolation(node, messageParams);
                }
            }
            return null;
        }


        @Override
        public final Void visitMethodOrCtor(ASTMethodOrConstructorDeclaration node, RuleContext ctx) {

            if (JavaOperationMetricKey.CYCLO.supports(node)) {
                int cyclo = (int) MetricsUtil.computeMetric(JavaOperationMetricKey.CYCLO, node, cycloOptions);
                if (cyclo >= methodReportLevel) {


                    String opname = PrettyPrintingUtil.displaySignature(node);

                    String kindname = node instanceof ASTConstructorDeclaration ? "constructor" : "method";


                    ctx.addViolation(node, new String[] {kindname,
                                                         opname,
                                                         "",
                                                         "" + cyclo,});
                }
            }
            return null;
        }
    }
}
