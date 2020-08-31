/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xml.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.xml.XmlLanguageModule;

public class XmlCPDTokenizerTest extends CpdTextComparisonTest {

    public XmlCPDTokenizerTest() {
        super(XmlLanguageModule.TERSE_NAME, ".xml");
    }

    @Test
    public void tokenizeTest() {
        doTest("simple");
    }
}
