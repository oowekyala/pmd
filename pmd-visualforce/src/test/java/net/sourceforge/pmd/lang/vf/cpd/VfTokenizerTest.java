/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.vf.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.vf.VfLanguageModule;

public class VfTokenizerTest extends CpdTextComparisonTest {

    public VfTokenizerTest() {
        super(VfLanguageModule.TERSE_NAME, ".page");
    }

    @Test
    public void testTokenize() {
        doTest("SampleUnescapeElWithTab");
    }
}
