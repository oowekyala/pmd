/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cs.cpd;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.ast.TokenMgrError;
import net.sourceforge.pmd.lang.cs.CsLanguage;

public class CsTokenizerTest extends CpdTextComparisonTest {

    @org.junit.Rule
    public ExpectedException ex = ExpectedException.none();

    public CsTokenizerTest() {
        super(CsLanguage.ID, ".cs");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/cs/cpd/testdata";
    }

    @Test
    public void testSimpleClass() {
        doTest("simpleClass");
    }

    @Test
    public void testSimpleClassMethodMultipleLines() {
        doTest("simpleClassMethodMultipleLines");
    }

    @Test
    public void testStrings() {
        doTest("strings");
    }

    @Test
    public void testOpenString() {
        ex.expect(TokenMgrError.class);
        doTest("unlexable_string");
    }

    @Test
    public void testCommentsIgnored1() {
        doTest("comments");
    }

    @Test
    public void testIgnoreBetweenSpecialComments() {
        doTest("specialComments");
    }

    @Test
    public void testOperators() {
        doTest("operatorsAndStuff");
    }


    @Test
    public void testLineNumberAfterMultilineString() {
        doTest("strings");
    }

    @Test
    public void testDoNotIgnoreUsingDirectives() {
        doTest("usingDirectives");
    }

    @Test
    public void testIgnoreUsingDirectives() {
        doTest("usingDirectives", "_ignored", ignoreUsings());
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }

    private CpdProperties ignoreUsings() {
        return properties(true);
    }

    private CpdProperties properties(boolean ignoreUsings) {
        return new CpdProperties().withProperty(Tokenizer.IGNORE_IMPORTS, ignoreUsings);
    }
}
