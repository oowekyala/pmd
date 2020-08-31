/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.lua.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.lua.LuaLanguage;

public class LuaTokenizerTest extends CpdTextComparisonTest {
    public LuaTokenizerTest() {
        super(LuaLanguage.ID, ".lua");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/lua/cpd/testdata";
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
