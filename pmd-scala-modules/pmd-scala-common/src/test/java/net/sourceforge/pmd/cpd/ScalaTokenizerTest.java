/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.ast.TokenMgrError;

public class ScalaTokenizerTest extends CpdTextComparisonTest {

    @org.junit.Rule
    public ExpectedException ex = ExpectedException.none();

    public ScalaTokenizerTest() {
        super(".scala");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/scala/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new ScalaTokenizer();
    }

    @Test
    public void testSample() {
        doTest("sample-LiftActor");
    }

    @Test
    public void tokenizeFailTest() {
        ex.expect(TokenMgrError.class);
        doTest("unlexable_sample");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }
}
