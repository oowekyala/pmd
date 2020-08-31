/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class RubyTokenizerTest extends CpdTextComparisonTest {

    public RubyTokenizerTest() {
        super(".rb");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/ruby/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new RubyTokenizer();
    }


    @Test
    public void testSimple() {
        doTest("server");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
