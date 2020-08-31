/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.jsp.JspLanguageModule;


public class JSPTokenizerTest extends CpdTextComparisonTest {

    public JSPTokenizerTest() {
        super(JspLanguageModule.TERSE_NAME, ".jsp");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/jsp/cpd/testdata";
    }

    @Test
    public void scriptletWithString() {
        doTest("scriptletWithString");
    }
}
