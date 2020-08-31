/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class GroovyTokenizerTest extends CpdTextComparisonTest {

    public GroovyTokenizerTest() {
        super(".groovy");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/groovy/cpd/testdata";
    }

    @Test
    public void testSample() {
        doTest("sample");
    }
}
