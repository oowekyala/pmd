/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.groovy.cpd;

import org.junit.Test;

import net.sourceforge.pmd.cpd.test.CpdTextComparisonTest;
import net.sourceforge.pmd.lang.groovy.GroovyLanguage;

public class GroovyTokenizerTest extends CpdTextComparisonTest {

    public GroovyTokenizerTest() {
        super(GroovyLanguage.ID, ".groovy");
    }

    @Test
    public void testSample() {
        doTest("sample");
    }
}
