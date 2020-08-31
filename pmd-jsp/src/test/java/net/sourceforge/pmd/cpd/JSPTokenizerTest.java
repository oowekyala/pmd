/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;


public class JSPTokenizerTest extends CpdTextComparisonTest {

    public JSPTokenizerTest() {
        super(".jsp");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/jsp/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new JSPTokenizer();
    }

    @Test
    public void scriptletWithString() {
        doTest("scriptletWithString");
    }
}
