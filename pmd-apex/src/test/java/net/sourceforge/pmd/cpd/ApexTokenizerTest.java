/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

public class ApexTokenizerTest extends CpdTextComparisonTest {

    public ApexTokenizerTest() {
        super("apex", ".cls");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/apex/cpd/testdata";
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
