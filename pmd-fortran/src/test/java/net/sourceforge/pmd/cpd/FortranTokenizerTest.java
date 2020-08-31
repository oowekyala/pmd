/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;

/**
 * @author rpelisse
 *
 */
public class FortranTokenizerTest extends CpdTextComparisonTest {

    public FortranTokenizerTest() {
        super(".for");
    }

    @Override
    protected String getResourcePrefix() {
        return "../lang/fortran/cpd/testdata";
    }

    @NotNull
    @Override
    public Tokenizer newTokenizer(@NotNull CpdProperties properties) {
        return new FortranTokenizer();
    }

    @Test
    public void testSample() {
        doTest("sample");
    }
}
