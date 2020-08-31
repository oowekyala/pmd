/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class LuaTokenizerTest extends CpdTextComparisonTest {
    public LuaTokenizerTest() {
        super(".lua");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/lua/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new LuaTokenizer();
    }

    @Test
    public void testSimple() {
        doTest("helloworld");
    }

    @Test
    public void testFactorial() {
        doTest("factorial");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
