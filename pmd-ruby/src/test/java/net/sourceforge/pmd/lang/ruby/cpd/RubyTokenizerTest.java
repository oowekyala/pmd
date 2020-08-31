/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ruby.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.ruby.RubyLanguage;

public class RubyTokenizerTest extends CpdTextComparisonTest {

    public RubyTokenizerTest() {
        super(RubyLanguage.ID, ".rb");
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
