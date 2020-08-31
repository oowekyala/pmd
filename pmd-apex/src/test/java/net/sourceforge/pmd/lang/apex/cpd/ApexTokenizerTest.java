/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.apex.ApexLanguageModule;

public class ApexTokenizerTest extends CpdTextComparisonTest {

    public ApexTokenizerTest() {
        super(ApexLanguageModule.TERSE_NAME, ".cls");
    }

    @Test
    public void testTokenize() {
        doTest("Simple");
    }

    @Test
    public void testTokenizeCaseSensitive() {
        doTest("Simple", "_caseSensitive", caseSensitive());
    }

    /**
     * Comments are ignored since using ApexLexer.
     */
    @Test
    public void testTokenizeWithComments() {
        doTest("comments");
    }

    @Test
    public void testTabWidth() {
        doTest("tabWidth");
    }

    private CpdProperties caseSensitive() {
        return properties(true);
    }

    private CpdProperties properties(boolean caseSensitive) {
        CpdProperties properties = new CpdProperties();
        properties.setProperty(Tokenizer.CASE_SENSITIVE, caseSensitive);
        return properties;
    }

}
