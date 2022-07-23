/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.testframework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.InputSource;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.renderers.TextRenderer;
import net.sourceforge.pmd.test.schema.RuleTestCollection;
import net.sourceforge.pmd.test.schema.TestSchemaParser;

/**
 * Advanced methods for test cases
 */
public abstract class RuleTst {

    protected void setUp() {
        // This method is intended to be overridden by subclasses.
    }

    protected List<Rule> getRules() {
        return Collections.emptyList();
    }

    /**
     * Find a rule in a certain ruleset by name
     *
     * todo make this static
     */
    public Rule findRule(String ruleSet, String ruleName) {
        try {
            Rule rule = RulesetsFactoryUtils.defaultFactory().createRuleSets(ruleSet).getRuleByName(ruleName);
            if (rule == null) {
                fail("Rule " + ruleName + " not found in ruleset " + ruleSet);
            } else {
                rule.setRuleSetName(ruleSet);
            }
            return rule;
        } catch (RuleSetNotFoundException e) {
            e.printStackTrace();
            fail("Couldn't find ruleset " + ruleSet);
            return null;
        }
    }

    /**
     * Run the rule on the given code, and check the expected number of
     * violations.
     */
    @SuppressWarnings("unchecked")
    @InternalApi
    @Deprecated
    public void runTest(TestDescriptor test) {
        Rule rule = test.getRule();

        // always reinitialize the rule, regardless of test.getReinitializeRule() (#3976 / #3302)
        rule = reinitializeRule(rule);

        Map<PropertyDescriptor<?>, Object> oldProperties = rule.getPropertiesByPropertyDescriptor();
        try {
            int res;
            Report report = new Report();
            try {
                // Set test specific properties onto the Rule
                if (test.getProperties() != null) {
                    for (Map.Entry<Object, Object> entry : test.getProperties().entrySet()) {
                        String propertyName = (String) entry.getKey();
                        PropertyDescriptor propertyDescriptor = rule.getPropertyDescriptor(propertyName);
                        if (propertyDescriptor == null) {
                            throw new IllegalArgumentException(
                                    "No such property '" + propertyName + "' on Rule " + rule.getName());
                        }

                        Object value = propertyDescriptor.valueFrom((String) entry.getValue());
                        rule.setProperty(propertyDescriptor, value);
                    }
                }

                runTestFromString(test.getCode(), rule, report, test.getLanguageVersion(), test.auxclasspathPrefix);
                res = report.size();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException('"' + test.getDescription() + "\" failed", e);
            }
            if (test.getNumberOfProblemsExpected() != res) {
                printReport(test, report);
            }
            assertEquals('"' + test.getDescription() + "\" resulted in wrong number of failures,",
                    test.getNumberOfProblemsExpected(), res);
            assertMessages(report, test);
            assertLineNumbers(report, test);
        } finally {
            // Restore old properties
            for (Map.Entry<PropertyDescriptor<?>, Object> entry : oldProperties.entrySet()) {
                rule.setProperty((PropertyDescriptor) entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * Code to be executed if the rule is reinitialised.
     *
     * @param rule The rule to reinitialise
     *
     * @return The rule once it has been reinitialised
     */
    protected Rule reinitializeRule(Rule rule) {
        return findRule(rule.getRuleSetName(), rule.getName());
    }


    private void assertMessages(Report report, TestDescriptor test) {
        if (report == null || test.getExpectedMessages().isEmpty()) {
            return;
        }

        List<String> expectedMessages = test.getExpectedMessages();
        if (report.size() != expectedMessages.size()) {
            throw new RuntimeException("Test setup error: number of expected messages doesn't match "
                    + "number of violations for test case '" + test.getDescription() + "'");
        }

        Iterator<RuleViolation> it = report.iterator();
        int index = 0;
        while (it.hasNext()) {
            RuleViolation violation = it.next();
            String actual = violation.getDescription();
            if (!expectedMessages.get(index).equals(actual)) {
                printReport(test, report);
            }
            assertEquals(
                    '"' + test.getDescription() + "\" produced wrong message on violation number " + (index + 1) + ".",
                    expectedMessages.get(index), actual);
            index++;
        }
    }

    private void assertLineNumbers(Report report, TestDescriptor test) {
        if (report == null || test.getExpectedLineNumbers().isEmpty()) {
            return;
        }

        List<Integer> expected = test.getExpectedLineNumbers();
        if (report.size() != expected.size()) {
            throw new RuntimeException("Test setup error: number of expected line numbers " + expected.size()
                    + " doesn't match number of violations " + report.size() + " for test case '"
                    + test.getDescription() + "'");
        }

        Iterator<RuleViolation> it = report.iterator();
        int index = 0;
        while (it.hasNext()) {
            RuleViolation violation = it.next();
            Integer actual = violation.getBeginLine();
            if (expected.get(index) != actual.intValue()) {
                printReport(test, report);
            }
            assertEquals('"' + test.getDescription() + "\" violation on wrong line number: violation number "
                    + (index + 1) + ".", expected.get(index), actual);
            index++;
        }
    }

    private void printReport(TestDescriptor test, Report report) {
        System.out.println("--------------------------------------------------------------");
        System.out.println("Test Failure: " + test.getDescription());
        System.out.println(" -> Expected " + test.getNumberOfProblemsExpected() + " problem(s), " + report.size()
                + " problem(s) found.");
        System.out.println(" -> Expected messages: " + test.getExpectedMessages());
        System.out.println(" -> Expected line numbers: " + test.getExpectedLineNumbers());
        System.out.println("Test Method Name: " + test.getTestMethodName());
        System.out.println("    @org.junit.Test public void " + test.getTestMethodName() + "() {}");
        System.out.println();
        TextRenderer renderer = new TextRenderer();
        renderer.setWriter(new StringWriter());
        try {
            renderer.start();
            renderer.renderFileReport(report);
            renderer.end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(renderer.getWriter().toString());
        System.out.println("--------------------------------------------------------------");
    }

    /**
     * Run the rule on the given code and put the violations in the report.
     */
    @InternalApi
    @Deprecated
    public void runTestFromString(String code, Rule rule, Report report, LanguageVersion languageVersion) {
        runTestFromString(code, rule, report, languageVersion, true);
    }

    @InternalApi
    @Deprecated
    public void runTestFromString(String code, Rule rule, Report report, LanguageVersion languageVersion,
                                  boolean isUseAuxClasspath) {
        runTestFromString(code, rule, report, languageVersion, null);
    }

    void runTestFromString(String code, Rule rule, Report report, LanguageVersion languageVersion,
                                  String auxClasspathPrefix) {
        try {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setDefaultLanguageVersion(languageVersion);
            configuration.setIgnoreIncrementalAnalysis(true);
            // regardless of isUseAuxClasspath the auxclasspath is always used (#3976 / #3302)
            // configure the "auxclasspath" option for unit testing
            if (auxClasspathPrefix != null) {
                // Since there is no platform independent classpath separator,
                // we use a comma in the xml.
                String prefixAsClasspath = auxClasspathPrefix.replace(',', File.pathSeparatorChar);
                configuration.prependAuxClasspath(prefixAsClasspath);
            }
            configuration.prependAuxClasspath(".");
            RuleContext ctx = new RuleContext();
            ctx.setReport(report);
            ctx.setSourceCodeFile(new File("n/a"));
            ctx.setLanguageVersion(languageVersion);
            ctx.setIgnoreExceptions(false);
            RuleSet rules = RuleSet.forSingleRule(rule);
            SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(configuration);
            sourceCodeProcessor.processSourceCode(new StringReader(code), new RuleSets(rules), ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @InternalApi
    @Deprecated
    public void runTestFromString(TestDescriptor test, Rule rule, Report report) {
        runTestFromString(test.getCode(), rule, report, test.getLanguageVersion(), test.isUseAuxClasspath());
    }

    /**
     * getResourceAsStream tries to find the XML file in weird locations if the
     * ruleName includes the package, so we strip it here.
     */
    @InternalApi
    @Deprecated
    protected String getCleanRuleName(Rule rule) {
        String fullClassName = rule.getClass().getName();
        if (fullClassName.equals(rule.getName())) {
            // We got the full class name, so we'll use the stripped name
            // instead
            String packageName = rule.getClass().getPackage().getName();
            return fullClassName.substring(packageName.length() + 1);
        } else {
            return rule.getName(); // Test is using findRule, smart!
        }
    }

    /**
     * Extract a set of tests from an XML file. The file should be
     * ./xml/RuleName.xml relative to the test class. The format is defined in
     * test-data.xsd.
     */
    @InternalApi
    @Deprecated
    public TestDescriptor[] extractTestsFromXml(Rule rule) {
        String testsFileName = getCleanRuleName(rule);

        return extractTestsFromXml(rule, testsFileName);
    }

    /**
     * Extract a set of tests from an XML file. The file should be
     * ./xml/RuleName.xml relative to the test class. The format is defined in
     * rule-tests_1_0_0.xsd in pmd-test-schema.
     */
    RuleTestCollection parseTestCollection(Rule rule) {
        String testsFileName = getCleanRuleName(rule);
        return parseTestXml(rule, testsFileName, "xml/");
    }

    @InternalApi
    @Deprecated
    public TestDescriptor[] extractTestsFromXml(Rule rule, String testsFileName) {
        return extractTestsFromXml(rule, testsFileName, "xml/");
    }

    /**
     * Extract a set of tests from an XML file with the given name. The file
     * should be ./xml/[testsFileName].xml relative to the test class. The
     * format is defined in test-data.xsd.
     */
    @InternalApi
    @Deprecated
    public TestDescriptor[] extractTestsFromXml(Rule rule, String testsFileName, String baseDirectory) {
        RuleTestCollection collection = parseTestXml(rule, testsFileName, baseDirectory);
        return toLegacyArray(collection);
    }

    private TestDescriptor[] toLegacyArray(RuleTestCollection collection) {
        TestDescriptor[] result = new TestDescriptor[collection.getTests().size()];
        for (int i = 0; i < collection.getTests().size(); i++) {
            result[i] = new TestDescriptor(collection.getTests().get(i));
        }
        return result;
    }

    /**
     * Extract a set of tests from an XML file with the given name. The file
     * should be ./xml/[testsFileName].xml relative to the test class. The
     * format is defined in test-data.xsd.
     */
    private RuleTestCollection parseTestXml(Rule rule, String testsFileName, String baseDirectory) {
        String testXmlFileName = baseDirectory + testsFileName + ".xml";

        try (InputStream inputStream = getClass().getResourceAsStream(testXmlFileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Couldn't find " + testXmlFileName);
            }
            InputSource source = new InputSource();
            source.setByteStream(inputStream);
            source.setSystemId(testXmlFileName);
            TestSchemaParser parser = new TestSchemaParser();
            return parser.parse(rule, source);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't parse " + testXmlFileName + ", due to: " + e, e);
        }
    }

    /**
     * Run a set of tests defined in an XML test-data file for a rule. The file
     * should be ./xml/RuleName.xml relative to the test-class. The format is
     * defined in test-data.xsd.
     */
    public void runTests(Rule rule) {
        runTests(extractTestsFromXml(rule));
    }

    /**
     * Run a set of tests defined in a XML test-data file. The file should be
     * ./xml/[testsFileName].xml relative to the test-class. The format is
     * defined in test-data.xsd.
     */
    public void runTests(Rule rule, String testsFileName) {
        runTests(extractTestsFromXml(rule, testsFileName));
    }

    /**
     * Run a set of tests of a certain sourceType.
     */
    @InternalApi
    @Deprecated
    public void runTests(TestDescriptor[] tests) {
        for (int i = 0; i < tests.length; i++) {
            runTest(tests[i]);
        }
    }

}
