/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class PLSQLTokenizerTest extends CpdTextComparisonTest {

    public PLSQLTokenizerTest() {
        super(".sql");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/plsql/cpd/testdata";
    }


    @Test
    public void testSimple() {
        doTest("sample-plsql");
    }

    @Test
    public void testSpecialComments() {
        doTest("specialComments");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
