/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContextTest;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.DummyNodeWithDeprecatedAttribute;
import net.sourceforge.pmd.lang.ast.DummyRoot;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

public class XPathRuleTest {

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().muteForSuccessfulTests().enableLog();

    @Test
    public void testAttributeDeprecation10() throws Exception {
        testDeprecation(XPathVersion.XPATH_1_0);
    }

    @Test
    public void testAttributeDeprecation20() throws Exception {
        testDeprecation(XPathVersion.XPATH_2_0);
    }


    public void testDeprecation(XPathVersion version) throws Exception {
        XPathRule xpr = makeRule(version, "SomeRule");

        systemErrRule.clearLog();

        DummyNode firstNode = newNode();

        // with another rule forked from the same one (in multithreaded processor)
        Report report = RuleContextTest.getReportForRuleApply(xpr, firstNode);
        assertEquals(1, report.getViolations().size());

        String log = systemErrRule.getLog();
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Size' by XPath rule 'SomeRule'"));
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Name' by XPath rule 'SomeRule', please use @Image instead"));


        systemErrRule.clearLog();

        // with another node
        report = RuleContextTest.getReportForRuleApply(xpr, newNode());

        assertEquals(1, report.getViolations().size());

        assertEquals("", systemErrRule.getLog()); // no additional warnings


        // with another rule forked from the same one (in multithreaded processor)
        report = RuleContextTest.getReportForRuleApply(xpr.deepCopy(), newNode());

        assertEquals(1, report.getViolations().size());

        assertEquals("", systemErrRule.getLog()); // no additional warnings

        // with another rule on the same node, new warnings
        XPathRule otherRule = makeRule(version, "OtherRule");
        otherRule.setRuleSetName("rset.xml");

        report = RuleContextTest.getReportForRuleApply(otherRule, firstNode);

        assertEquals(1, report.getViolations().size());

        log = systemErrRule.getLog();
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Size' by XPath rule 'OtherRule' (in ruleset 'rset.xml')"));
        assertThat(log, Matchers.containsString("Use of deprecated attribute 'dummyNode/@Name' by XPath rule 'OtherRule' (in ruleset 'rset.xml'), please use @Image instead"));

    }

    public XPathRule makeRule(XPathVersion version, String name) {
        XPathRule xpr = new XPathRule(version, "//dummyNode[@Size >= 2 and @Name='foo']");
        xpr.setName(name);
        xpr.setLanguage(LanguageRegistry.getLanguage("Dummy"));
        xpr.setMessage("gotcha");
        return xpr;
    }

    public DummyNode newNode() {
        DummyRoot root = new DummyRoot();
        DummyNode dummy = new DummyNodeWithDeprecatedAttribute();
        root.addChild(dummy, 0);
        dummy.setCoords(1, 1, 1, 2);
        return root;
    }
}
