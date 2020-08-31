/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.swift.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.swift.SwiftLanguageModule;

public class SwiftTokenizerTest extends CpdTextComparisonTest {

    public SwiftTokenizerTest() {
        super(SwiftLanguageModule.TERSE_NAME, ".swift");
    }

    @Test
    public void testSwift42() {
        doTest("Swift4.2");
    }

    @Test
    public void testSwift50() {
        doTest("Swift5.0");
    }

    @Test
    public void testSwift51() {
        doTest("Swift5.1");
    }

    @Test
    public void testSwift52() {
        doTest("Swift5.2");
    }

    @Test
    public void testStackoverflowOnLongLiteral() {
        doTest("Issue628");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
