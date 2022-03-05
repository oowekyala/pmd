/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper;
import net.sourceforge.pmd.lang.ast.test.BaseTreeDumpTest;
import net.sourceforge.pmd.lang.ast.test.RelevantAttributePrinter;
import net.sourceforge.pmd.lang.java.JavaParsingHelper;

public class Java17PreviewTreeDumpTest extends BaseTreeDumpTest {
    private final JavaParsingHelper java17p =
            JavaParsingHelper.DEFAULT.withDefaultVersion("17-preview")
                                     .withResourceContext(Java17PreviewTreeDumpTest.class, "jdkversiontests/java17p/");
    private final JavaParsingHelper java17 = java17p.withDefaultVersion("17");

    public Java17PreviewTreeDumpTest() {
        super(new RelevantAttributePrinter(), ".java");
    }

    @Override
    public BaseParsingHelper<?, ?> getParser() {
        return java17p;
    }

    @Test
    public void patternMatchingForSwitchBeforeJava17Preview() {
        ParseException thrown = Assert.assertThrows(ParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                java17.parseResource("PatternsInSwitchLabels.java");
            }
        });
        Assert.assertTrue("Unexpected message: " + thrown.getMessage(),
                thrown.getMessage().contains("Pattern matching for switch is a preview feature of JDK 17, you should select your language version accordingly"));
    }

    @Test
    public void patternMatchingForSwitch() {
        doTest("PatternsInSwitchLabels");
    }

    @Test
    public void enhancedTypeCheckingSwitch() {
        doTest("EnhancedTypeCheckingSwitch");
    }

    @Test
    public void scopeOfPatternVariableDeclarations() {
        doTest("ScopeOfPatternVariableDeclarations");
    }

    @Test
    public void dealingWithNullBeforeJava17Preview() {
        ParseException thrown = Assert.assertThrows(ParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                java17.parseResource("DealingWithNull.java");
            }
        });
        Assert.assertTrue("Unexpected message: " + thrown.getMessage(),
                thrown.getMessage().contains("Null case labels is a preview feature of JDK 17, you should select your language version accordingly"));
    }

    @Test
    public void dealingWithNull() {
        doTest("DealingWithNull");
    }

    @Test
    public void guardedAndParenthesizedPatternsBeforeJava17Preview() {
        ParseException thrown = Assert.assertThrows(ParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                java17.parseResource("GuardedAndParenthesizedPatterns.java");
            }
        });
        assertThat(thrown.getMessage(), containsString("Pattern matching for switch is a preview feature of JDK 17, you should select your language version accordingly"));
    }

    @Test
    public void guardedAndParenthesizedPatterns() {
        doTest("GuardedAndParenthesizedPatterns");
    }
}
