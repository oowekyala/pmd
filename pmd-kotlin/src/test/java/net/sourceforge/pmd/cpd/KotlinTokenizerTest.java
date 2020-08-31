/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class KotlinTokenizerTest extends CpdTextComparisonTest {

    public KotlinTokenizerTest() {
        super(".kt");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/kotlin/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new KotlinTokenizer();
    }

    @Test
    public void testComments() {
        doTest("comment");
    }

    @Test
    public void testIncrement() {
        doTest("increment");
    }

    @Test
    public void testImportsIgnored() {
        doTest("imports");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
