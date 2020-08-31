/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.plsql.PLSQLLanguageModule;

public class PLSQLTokenizerTest extends CpdTextComparisonTest {

    public PLSQLTokenizerTest() {
        super(PLSQLLanguageModule.TERSE_NAME, ".sql");
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
