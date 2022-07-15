/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli;

import static net.sourceforge.pmd.util.CollectionUtil.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.PMDConfiguration;

public class PMDParametersTest {

    @Test
    public void testVersion() throws Exception {
        PMDParameters parameters = new PMDParameters();
        // no language set, uses default language
        Assert.assertEquals("1.7", parameters.getVersion());

        // now set language
        FieldUtils.writeDeclaredField(parameters, "language", "dummy2", true);
        Assert.assertEquals("1.0", parameters.getVersion());
    }

    @Test
    public void testMultipleDirsAndRuleSets() {
        PmdParametersParseResult result = PmdParametersParseResult.extractParameters(
            "-d", "a", "b", "-R", "x.xml", "y.xml"
        );
        assertMultipleDirsAndRulesets(result);
    }

    @Test
    public void testMultipleDirsAndRuleSetsWithCommas() {
        PmdParametersParseResult result = PmdParametersParseResult.extractParameters(
            "-d", "a,b", "-R", "x.xml,y.xml"
        );
        assertMultipleDirsAndRulesets(result);
    }

    @Test
    public void testMultipleDirsAndRuleSetsWithRepeatedOption() {
        PmdParametersParseResult result = PmdParametersParseResult.extractParameters(
            "-d", "a", "-d", "b", "-R", "x.xml", "-R", "y.xml"
        );
        assertMultipleDirsAndRulesets(result);
    }

    @Test
    public void testNoPositionalParametersAllowed() {
        assertError(
            //                        vvvv
            "-R", "x.xml", "-d", "a", "--", "-d", "b"
        );
    }


    private void assertMultipleDirsAndRulesets(PmdParametersParseResult result) {
        assertFalse(result.isError());
        PMDConfiguration config = result.toConfiguration();
        assertEquals(config.getAllInputPaths(), listOf("a", "b"));
        assertEquals(config.getRuleSetPaths(), listOf("x.xml", "y.xml"));
    }

    @Test
    public void testEmptyDirOption() {
        assertError("-d", "-R", "y.xml");
    }

    @Test
    public void testEmptyRulesetOption() {
        assertError("-R", "-d", "something");
    }

    private void assertError(String... params) {
        PmdParametersParseResult result = PmdParametersParseResult.extractParameters(params);
        assertTrue(result.isError());
    }

}
