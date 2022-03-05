/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMD.StatusCode;
import net.sourceforge.pmd.internal.Slf4jSimpleConfiguration;

/**
 *
 */
public class CoreCliTest {

    private static final String DUMMY_RULESET = "net/sourceforge/pmd/cli/FakeRuleset.xml";
    private static final String STRING_TO_REPLACE = "__should_be_replaced__";

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    // restoring system properties: -debug might change logging properties
    // See Slf4jSimpleConfigurationForAnt and resetLogging
    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();
    @Rule
    public final SystemOutRule outStreamCaptor = new SystemOutRule().muteForSuccessfulTests().enableLog();
    @Rule
    public final SystemErrRule errStreamCaptor = new SystemErrRule().muteForSuccessfulTests().enableLog();

    @AfterClass
    public static void resetLogging() {
        Slf4jSimpleConfiguration.reconfigureDefaultLogLevel(null);
    }

    private Path srcDir;

    @Before
    public void setup() throws IOException {
        // set current directory to wd
        Path root = tempRoot();
        System.setProperty("user.dir", root.toString());

        // create a few files
        srcDir = Files.createDirectories(root.resolve("src"));
        writeString(srcDir.resolve("someSource.dummy"), "dummy text");
    }


    @Test
    public void testPreExistingReportFile() throws IOException {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");
        // now we create the file
        Files.createDirectories(reportFile.getParent());
        writeString(reportFile, STRING_TO_REPLACE);

        assertTrue("Report file should exist", Files.exists(reportFile));

        runPmdSuccessfully("-no-cache", "-d", srcDir, "-R", DUMMY_RULESET, "-r", reportFile);

        assertNotEquals(readString(reportFile), STRING_TO_REPLACE);
    }

    @Test
    public void testPreExistingReportFileLongOption() throws IOException {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");
        // now we create the file
        Files.createDirectories(reportFile.getParent());
        writeString(reportFile, STRING_TO_REPLACE);

        assertTrue("Report file should exist", Files.exists(reportFile));

        runPmdSuccessfully("--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET, "--report-file", reportFile);

        assertNotEquals("Report file should have been overwritten", readString(reportFile), STRING_TO_REPLACE);
    }

    @Test
    public void testNonExistentReportFile() throws IOException {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");

        assertFalse("Report file should not exist", Files.exists(reportFile));

        try {
            runPmdSuccessfully("-no-cache", "-d", srcDir, "-R", DUMMY_RULESET, "-r", reportFile);
            assertTrue("Report file should have been created", Files.exists(reportFile));
        } finally {
            Files.deleteIfExists(reportFile);
        }
    }

    @Test
    public void testNonExistentReportFileLongOption() {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");

        assertFalse("Report file should not exist", Files.exists(reportFile));

        runPmdSuccessfully("--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET, "--report-file", reportFile);

        assertTrue("Report file should have been created", Files.exists(reportFile));
    }

    @Test
    public void testFileCollectionWithUnknownFiles() throws IOException {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");
        Files.createFile(srcDir.resolve("foo.not_analysable"));
        assertFalse("Report file should not exist", Files.exists(reportFile));

        runPmdSuccessfully("--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET, "--report-file", reportFile, "--debug");

        assertTrue("Report file should have been created", Files.exists(reportFile));
        String reportText = IOUtils.toString(Files.newBufferedReader(reportFile, StandardCharsets.UTF_8));
        assertThat(reportText, not(containsStringIgnoringCase("error")));
    }

    @Test
    public void testNonExistentReportFileDeprecatedOptions() {
        Path reportFile = tempRoot().resolve("out/reportFile.txt");

        assertFalse("Report file should not exist", Files.exists(reportFile));

        runPmdSuccessfully("-no-cache", "-dir", srcDir, "-rulesets", DUMMY_RULESET, "-reportfile", reportFile);

        assertTrue("Report file should have been created", Files.exists(reportFile));
        assertTrue(errStreamCaptor.getLog().contains("Some deprecated options were used on the command-line, including -rulesets"));
        assertTrue(errStreamCaptor.getLog().contains("Consider replacing it with --rulesets (or -R)"));
        // only one parameter is logged
        assertFalse(errStreamCaptor.getLog().contains("Some deprecated options were used on the command-line, including -reportfile"));
        assertFalse(errStreamCaptor.getLog().contains("Consider replacing it with --report-file"));
    }

    /**
     * This tests to create the report file in the current working directory.
     *
     * <p>Note: We can't change the cwd in the running VM, so the file will not be created
     * in the temporary folder, but really in the cwd. The test fails if a file already exists
     * and makes sure to cleanup the file afterwards.
     */
    @Test
    public void testRelativeReportFile() throws IOException {
        String reportFile = "reportFile.txt";
        Path absoluteReportFile = FileSystems.getDefault().getPath(reportFile).toAbsolutePath();
        // verify the file doesn't exist yet - we will delete the file at the end!
        assertFalse("Report file must not exist yet!", Files.exists(absoluteReportFile));

        try {
            runPmdSuccessfully("-no-cache", "-d", srcDir, "-R", DUMMY_RULESET, "-r", reportFile);
            assertTrue("Report file should have been created", Files.exists(absoluteReportFile));
        } finally {
            Files.deleteIfExists(absoluteReportFile);
        }
    }

    @Test
    public void testRelativeReportFileLongOption() throws IOException {
        String reportFile = "reportFile.txt";
        Path absoluteReportFile = FileSystems.getDefault().getPath(reportFile).toAbsolutePath();
        // verify the file doesn't exist yet - we will delete the file at the end!
        assertFalse("Report file must not exist yet!", Files.exists(absoluteReportFile));

        try {
            runPmdSuccessfully("--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET, "--report-file", reportFile);
            assertTrue("Report file should have been created", Files.exists(absoluteReportFile));
        } finally {
            Files.deleteIfExists(absoluteReportFile);
        }
    }

    @Test
    public void debugLogging() {
        runPmdSuccessfully("--debug", "--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET);
        assertThat(errStreamCaptor.getLog(), containsString("[main] INFO net.sourceforge.pmd.PMD - Log level is at TRACE"));
    }

    @Test
    public void defaultLogging() {
        runPmdSuccessfully("--no-cache", "--dir", srcDir, "--rulesets", DUMMY_RULESET);
        assertThat(errStreamCaptor.getLog(), containsString("[main] INFO net.sourceforge.pmd.PMD - Log level is at INFO"));
    }


    @Test
    public void testWrongCliOptionsDoNotPrintUsage() {
        String[] args = { "-invalid" };
        PmdParametersParseResult params = PmdParametersParseResult.extractParameters(args);
        assertTrue("Expected invalid args", params.isError());

        StatusCode code = PMD.runPmd(args);
        assertEquals(StatusCode.ERROR, code);
        assertThatErrAndOut(not(containsStringIgnoringCase("Available report formats and")));
    }

    // utilities

    private void assertThatErrAndOut(Matcher<String> matcher) {
        assertThat("stdout", outStreamCaptor.getLog(), matcher);
        assertThat("stderr", errStreamCaptor.getLog(), matcher);
    }

    private Path tempRoot() {
        return tempDir.getRoot().toPath();
    }


    private static void runPmdSuccessfully(Object... args) {
        runPmd(0, args);
    }

    private static String[] argsToString(Object... args) {
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i].toString();
        }
        return result;
    }

    // available in Files on java 11+
    private static void writeString(Path path, String text) throws IOException {
        ByteBuffer encoded = StandardCharsets.UTF_8.encode(text);
        Files.write(path, encoded.array());
    }


    // available in Files on java 11+
    private static String readString(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return StandardCharsets.UTF_8.decode(buf).toString();
    }

    private static void runPmd(int expectedExitCode, Object[] args) {
        StatusCode actualExitCode = PMD.runPmd(argsToString(args));
        assertEquals("Exit code", expectedExitCode, actualExitCode.toInt());
    }


}
