/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import net.sourceforge.pmd.PMD.StatusCode;
import net.sourceforge.pmd.internal.Slf4jSimpleConfiguration;

/**
 * @author Romain Pelisse &lt;belaran@gmail.com&gt;
 *
 */
public class CLITest extends BaseCLITest {

    // restoring system properties: -debug might change logging properties
    // See Slf4jSimpleConfigurationForAnt and resetLogging
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @AfterClass
    public static void resetLogging() {
        Slf4jSimpleConfiguration.reconfigureDefaultLogLevel(null);
    }

    @Before
    public void setupLogging() {
        Slf4jSimpleConfiguration.reconfigureDefaultLogLevel(null);
    }


    @Test
    public void minimalArgs() {
        runTest("-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/bestpractices.xml,category/java/design.xml");
    }

    @Test
    public void minimumPriority() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml", "-min", "1", };
        runTest(args);
    }

    @Test
    public void usingDebug() {
        runTest("-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml", "-debug");
    }

    @Test
    public void usingDebugLongOption() {
        runTest("-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml", "--debug");
    }

    @Test
    public void changeJavaVersion() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml", "-version", "1.5", "-language",
                          "java", "--debug", };
        String log = runTest(args);
        assertThat(log, containsPattern("Adding file .*\\.java \\(lang: java 1\\.5\\)"));
    }

    @Test
    public void exitStatusNoViolations() {
        runTest("-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml");
    }

    @Test
    public void exitStatusWithViolations() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/errorprone.xml", };
        String log = runTest(StatusCode.VIOLATIONS_FOUND, args);
        assertThat(log, containsString("Avoid empty if"));
    }

    @Test
    public void exitStatusWithViolationsAndWithoutFailOnViolations() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/errorprone.xml", "-failOnViolation", "false", };
        String log = runTest(StatusCode.OK, args);
        assertThat(log, containsString("Avoid empty if"));
    }

    @Test
    public void exitStatusWithViolationsAndWithoutFailOnViolationsLongOption() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/errorprone.xml", "--fail-on-violation", "false", };
        String log = runTest(StatusCode.OK, args);
        assertThat(log, containsString("Avoid empty if"));
    }

    /**
     * See https://sourceforge.net/p/pmd/bugs/1231/
     */
    @Test
    public void testWrongRuleset() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/designn.xml", };
        String log = runTest(StatusCode.ERROR, args);
        assertThat(log, containsString("Can't find resource 'category/java/designn.xml' for rule 'null'."
                                           + "  Make sure the resource is a valid file"));
    }

    /**
     * See https://sourceforge.net/p/pmd/bugs/1231/
     */
    @Test
    public void testWrongRulesetWithRulename() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/designn.xml/UseCollectionIsEmpty", };
        String log = runTest(StatusCode.ERROR, args);
        assertThat(log, containsString("Can't find resource 'category/java/designn.xml' for rule "
                                           + "'UseCollectionIsEmpty'."));
    }

    /**
     * See https://sourceforge.net/p/pmd/bugs/1231/
     */
    @Test
    public void testWrongRulename() {
        String[] args = { "-d", SOURCE_FOLDER, "-f", "text", "-R", "category/java/design.xml/ThisRuleDoesNotExist", };
        String log = runTest(StatusCode.OK, args);
        assertThat(log, containsString("No rules found. Maybe you misspelled a rule name?"
                                           + " (category/java/design.xml/ThisRuleDoesNotExist)"));
    }
}
