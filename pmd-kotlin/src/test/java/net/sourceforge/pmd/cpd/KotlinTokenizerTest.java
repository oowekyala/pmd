/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class KotlinTokenizerTest extends CpdTextComparisonTest {

    public KotlinTokenizerTest() {
        super(".kt");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/kotlin/cpd/testdata";
    }

    @Test
    public void testComments() {
        doTest("comment");
    }

    @Test
    public void testIncrement() {
        doTest("increment");
    }

    @Test
    public void testImportsIgnored() {
        doTest("imports");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
