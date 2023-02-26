/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.it;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.PMDVersion;

class BinaryDistributionIT extends AbstractBinaryDistributionTest {

    private static final String SUPPORTED_LANGUAGES_CPD;
    private static final String SUPPORTED_LANGUAGES_PMD;

    static {
        SUPPORTED_LANGUAGES_CPD = "Valid values: apex, cpp, cs, dart, ecmascript," + System.lineSeparator()
                + "                               fortran, gherkin, go, groovy, html, java, jsp," + System.lineSeparator()
                + "                               kotlin, lua, matlab, modelica, objectivec, perl," + System.lineSeparator()
                + "                               php, plsql, python, ruby, scala, swift, tsql," + System.lineSeparator()
                + "                               vf, xml";
        SUPPORTED_LANGUAGES_PMD = "Valid values: apex-54, ecmascript-ES6, html-," + System.lineSeparator()
                + "                              java-1.10, java-1.3, java-1.4, java-1.5, java-1." + System.lineSeparator()
                + "                              6, java-1.7, java-1.8, java-1.9, java-10," + System.lineSeparator()
                + "                              java-11, java-12, java-13, java-14, java-15," + System.lineSeparator()
                + "                              java-16, java-17, java-18, java-19," + System.lineSeparator()
                + "                              java-19-preview, java-20, java-20-preview," + System.lineSeparator()
                + "                              java-5, java-6, java-7, java-8, java-9, jsp-," + System.lineSeparator()
                + "                              kotlin-1.6, kotlin-1.6-rfc+0.1, modelica-," + System.lineSeparator()
                + "                              plsql-, pom-, scala-2.10, scala-2.11, scala-2.12," + System.lineSeparator()
                + "                              scala-2.13, swift-, vf-, vm-, wsdl-, xml-, xsl-";
    }

    private final String srcDir = new File(".", "src/test/resources/sample-source/java/").getAbsolutePath();

    @Test
    void testFileExistence() {
        assertTrue(getBinaryDistribution().exists());
    }

    private Set<String> getExpectedFileNames() {
        Set<String> result = new HashSet<>();
        String basedir = "pmd-bin-" + PMDVersion.VERSION + "/";
        result.add(basedir);
        result.add(basedir + "LICENSE");
        result.add(basedir + "bin/pmd");
        result.add(basedir + "bin/pmd.bat");
        result.add(basedir + "conf/simplelogger.properties");
        result.add(basedir + "shell/pmd-completion.sh");
        result.add(basedir + "lib/pmd-core-" + PMDVersion.VERSION + ".jar");
        result.add(basedir + "lib/pmd-java-" + PMDVersion.VERSION + ".jar");
        return result;
    }

    @Test
    void testZipFileContent() throws IOException {
        Set<String> expectedFileNames = getExpectedFileNames();

        ZipFile zip = new ZipFile(getBinaryDistribution());

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            expectedFileNames.remove(entry.getName());
        }

        zip.close();

        if (!expectedFileNames.isEmpty()) {
            fail("Missing files in archive: " + expectedFileNames);
        }
    }

    @Test
    void testPmdJavaQuickstart() throws Exception {
        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir, "rulesets/java/quickstart.xml");
        result.assertExecutionResult(4, "");
    }

    @Test
    void testPmdXmlFormat() throws Exception {
        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir, "src/test/resources/rulesets/sample-ruleset.xml", "xml");
        result.assertExecutionResult(4, "", "JumbledIncrementer.java\">");
        result.assertExecutionResult(4, "", "<violation beginline=\"8\" endline=\"10\" begincolumn=\"13\" endcolumn=\"14\" rule=\"JumbledIncrementer\"");
    }

    @Test
    void testPmdSample() throws Exception {
        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir, "src/test/resources/rulesets/sample-ruleset.xml");
        result.assertExecutionResult(4, "", "JumbledIncrementer.java:8:");
    }

    @Test
    void testPmdSampleWithZippedSources() throws Exception {
        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir + "/sample-source-java.zip",
                "src/test/resources/rulesets/sample-ruleset.xml");
        result.assertExecutionResult(4, "", "JumbledIncrementer.java:8:");
    }

    @Test
    void testPmdSampleWithJarredSources() throws Exception {
        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir + "/sample-source-java.jar",
                "src/test/resources/rulesets/sample-ruleset.xml");
        result.assertExecutionResult(4, "", "JumbledIncrementer.java:8:");
    }

    @Test
    void testPmdHelp() throws Exception {
        ExecutionResult result = PMDExecutor.runPMD(null, tempDir, "-h");
        result.assertExecutionResult(0, SUPPORTED_LANGUAGES_PMD);
    }

    @Test
    void testPmdNoArgs() throws Exception {
        ExecutionResult result = PMDExecutor.runPMD(null, tempDir); // without any argument, display usage help and error
        result.assertExecutionResultErrOutput(2, "Usage: pmd check ");
    }

    @Test
    void logging() throws Exception {
        String srcDir = new File(".", "src/test/resources/sample-source/java/").getAbsolutePath();

        ExecutionResult result;

        result = PMDExecutor.runPMD(createTemporaryReportFile(), tempDir, "-d", srcDir, "-R", "src/test/resources/rulesets/sample-ruleset.xml");
        result.assertExecutionResult(4);
        result.assertErrorOutputContains("[main] INFO net.sourceforge.pmd.cli.commands.internal.AbstractPmdSubcommand - Log level is at INFO");


        // now with debug
        result = PMDExecutor.runPMD(createTemporaryReportFile(), tempDir, "-d", srcDir, "-R", "src/test/resources/rulesets/sample-ruleset.xml", "--debug");
        result.assertExecutionResult(4);
        result.assertErrorOutputContains("[main] INFO net.sourceforge.pmd.cli.commands.internal.AbstractPmdSubcommand - Log level is at TRACE");
    }

    @Test
    void runPMDWithError() throws Exception {
        String srcDir = new File(".", "src/test/resources/sample-source/unparsable/").getAbsolutePath();

        ExecutionResult result = PMDExecutor.runPMDRules(createTemporaryReportFile(), tempDir, srcDir, "src/test/resources/rulesets/sample-ruleset.xml");
        result.assertExecutionResultErrOutput(0, "Run in verbose mode to see a stack-trace.");
    }

    @Test
    void runCPD() throws Exception {
        String srcDir = new File(".", "src/test/resources/sample-source-cpd/").getAbsolutePath();

        ExecutionResult result;

        result = CpdExecutor.runCpd(tempDir); // without any argument, display usage help and error
        result.assertExecutionResultErrOutput(2, "Usage: pmd cpd ");

        result = CpdExecutor.runCpd(tempDir, "-h");
        result.assertExecutionResult(0, SUPPORTED_LANGUAGES_CPD);

        result = CpdExecutor.runCpd(tempDir, "--minimum-tokens", "10", "--format", "text", "--dir", srcDir);
        result.assertExecutionResult(4, "Found a 10 line (55 tokens) duplication in the following files:");
        result.assertExecutionResult(4, "Class1.java");
        result.assertExecutionResult(4, "Class2.java");

        result = CpdExecutor.runCpd(tempDir, "--minimum-tokens", "10", "--format", "xml", "--dir", srcDir);
        result.assertExecutionResult(4, "<duplication lines=\"10\" tokens=\"55\">");
        result.assertExecutionResult(4, "Class1.java\"/>");
        result.assertExecutionResult(4, "Class2.java\"/>");

        result = CpdExecutor.runCpd(tempDir, "--minimum-tokens", "1000", "--format", "text", "--dir", srcDir);
        result.assertExecutionResult(0);
    }
}
