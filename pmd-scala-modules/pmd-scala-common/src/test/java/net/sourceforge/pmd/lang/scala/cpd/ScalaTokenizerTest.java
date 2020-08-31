/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.scala.cpd;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.lang.scala.ScalaLanguageModule;

public class ScalaTokenizerTest extends CpdTextComparisonTest {

    @org.junit.Rule
    public ExpectedException ex = ExpectedException.none();

    public ScalaTokenizerTest() {
        super(ScalaLanguageModule.TERSE_NAME, ".scala");
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
