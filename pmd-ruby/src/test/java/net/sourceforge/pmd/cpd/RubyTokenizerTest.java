/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class RubyTokenizerTest extends CpdTextComparisonTest {

    public RubyTokenizerTest() {
        super(".rb");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/ruby/cpd/testdata";
    }


    @Test
    public void testSimple() {
        doTest("server");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
