/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.objc.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.objc.ObjectiveCLanguage;

public class ObjectiveCTokenizerTest extends CpdTextComparisonTest {

    public ObjectiveCTokenizerTest() {
        super(ObjectiveCLanguage.ID, ".m");
    }

    @Test
    public void testLongSample() {
        doTest("big_sample");
    }

    @Test
    public void testUnicodeEscape() {
        doTest("unicodeEscapeInString");
    }

    @Test
    public void testUnicodeCharInIdent() {
        doTest("unicodeCharInIdent");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
