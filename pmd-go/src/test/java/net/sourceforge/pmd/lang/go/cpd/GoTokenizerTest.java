/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.go.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.go.GoLanguage;

public class GoTokenizerTest extends CpdTextComparisonTest {

    public GoTokenizerTest() {
        super(GoLanguage.TERSE_NAME, ".go");
    }

    @Test
    public void simpleTest() {
        doTest("hello");
    }

    @Test
    public void bigFileTest() {
        doTest("btrfs");
    }

    @Test
    public void testIssue1751() {
        doTest("issue-1751");
    }
}
