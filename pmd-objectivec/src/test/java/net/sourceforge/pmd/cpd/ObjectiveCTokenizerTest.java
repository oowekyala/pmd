/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class ObjectiveCTokenizerTest extends CpdTextComparisonTest {


    public ObjectiveCTokenizerTest() {
        super(".m");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/objc/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new ObjectiveCTokenizer();
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
